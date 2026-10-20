# UI/UX Design System v5 Implementation Plan

> **STATUS: READY** — written 2026-10-19. Canonical design source:
> `docs/03_design_system/uiux_v5/` (supersedes v4 per its README).
> Queued as **Q16b** in `2026-10-18-droidkit-master-execution-plan.md`.
> Tracker section: `01_plans/10_DroidKit_Master_Journey_Checklist.md` →
> "UI/UX V5 Design System Pass".

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the v5 design system (Material 3 + Minimalism + Dark Technical + Bento + Green/Teal Aurora + restrained Glass) across the app: production brand assets, semantic tokens, shared components, friendly-error system, adaptive shell, and a per-feature UI migration pass — incrementally, never one giant redesign.

**Architecture:** All work lands in or consumes `:core:ui`. New semantic tokens ride on top of M3 via composition locals inside `DroidKitTheme`. Screen state and availability come from existing contracts (`CapabilityManager`, `PermissionManager`, `ToolResolution`/`BlockedReason`, `TestResultState`, ViewModel `UiState`) — no new architecture, no feature→privilege imports, no business-logic rewrites.

**Tech Stack:** Kotlin, Compose Material 3, `androidx.core:core-splashscreen`, existing `:core:ui` module (`io.github.droidkit.core.ui`).

## Global Constraints

- Read order for every task: `docs/03_design_system/uiux_v5/AI_UI_UX_V5_HANDOFF.md` → `foundation/` → `semantics/` → relevant `screens/` → `implementation/`. Where v5 is terse, v4 docs (`docs/03_design_system/uiux_v4_superseded/docs/`) provide detail — v5 wins any conflict.
- Rules: root `AGENTS.md`; per-slice gate = v5 `implementation/22_DEFINITION_OF_DONE.md` (reproduced as the checklist under Task 11).
- Honesty: never fabricate values; `NOT_SUPPORTED`/`PERMISSION_REQUIRED`/`UNKNOWN` are never rendered as `FAIL`; unavailable data shows `Unavailable`/`Not exposed`, never blank/0.
- Status is never color-only: icon + label + color, always.
- Errors: never a raw exception as primary copy; sanitized technical details behind progressive disclosure; no secrets on screen.
- Shared UI (`:core:ui`) never calls repositories/system APIs/Groq/Shizuku/ADB directly.
- User-facing strings land in Android resources (`res/values/strings*.xml`), not scattered literals.
- Package root `io.github.droidkit`; minSdk 28 / target 36; JDK: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- Commit per task; push to `origin` after every commit (root `AGENTS.md` rule). Smallest coherent change; no unrelated edits.
- Targeted validation only: identify changed files first; run the module's tests + `:app:assembleDebug`; broaden only when shared contracts changed (Tasks 2–6 change `:core:ui`, which every feature consumes — run `:app:assembleDebug` after each of those).

## Current State (audit snapshot, verified 2026-10-19)

- `core/ui/.../theme/Brand.kt` — `DroidKitBrand` colors + `DroidKitMark` vector (Candidate-4 hexagon/capsule mark at audit time; superseded by Candidate 9 glowing-head mark on 2026-10-20 — see `docs/03_design_system/branding/`).
- `core/ui/.../theme/Theme.kt` — `DroidKitTheme(darkTheme, dynamicColor = true)` wrapping raw M3 schemes. **No semantic token layer, dynamic color ON by default (conflicts with v5 brand identity).**
- `core/ui/.../components/` — `DroidKitWordmark`, `Feedback.kt` (`SectionHeader`, `EmptyState`, `BlockedReason.describe()`, `BlockedReasonList`), `ToolCard`.
- `core/ui/` root — `FeatureStubScreen`, `InfoRow`, `LabIndexScreen`, `SectionCard`.
- Existing types to reuse: `TestResultState` (PASS/FAIL/NOT_TESTED/NOT_SUPPORTED/PERMISSION_REQUIRED/UNKNOWN) and `SafetyLevel` in `:core:common`; `ToolResolution` (`Ready`/`Blocked`) + `BlockedReason` (`MissingPermission`/`MissingHardware`/`RequiresApi`/`NeedsProvider`) + `ProviderKind`/`ProviderStatus` in `:core:privileges`.
- Gaps vs v5: no dark-first graphite scheme, no spacing/shape/typography token objects in code (values exist only in `docs/02_foundations/j03_design/J03_*`), no Loading/Permission/SetupRequired/Unsupported/Offline/FriendlyError state components, no error mapper, no risk/AI components, no adaptive nav (bottom-nav only), no bento Home, default launcher icon (no production assets), no `core-splashscreen`.

## v4 → v5 Change Reference

| Area | v4 | v5 | Migration note |
|---|---|---|---|
| Doc layout | Flat `docs/` + `docs/screens/` (13 files) | `foundation/` `semantics/` `screens/` `implementation/` (16 files) | Paths changed; handoff doc is `AI_UI_UX_V5_HANDOFF.md` |
| Design language | Same 6 ingredients | Same, plus explicit "not primary" list (Neumorphism/Cyberpunk/Brutalism/etc.) and "decoration decreases as density increases" | No rework of v4-correct work; stricter on when Bento/Aurora/Glass apply |
| Tokens | `02_DESIGN_TOKENS.md` detailed tables | `02_TOKENS_COMPONENTS.md` names only + 28-component list | Token **values** come from `docs/02_foundations/j03_design/J03_DESIGN_TOKENS.md` + this plan; v5 names are canonical |
| Errors | `07_MICROCOPY_STATUS_LANGUAGE.md` | Split: `04_FRIENDLY_ERRORS.md` (error anatomy) + `06_ERROR_CATALOG.md` (per-domain strings) + `07_MICROCOPY.md` | New deliverable: `FriendlyError` model + mapper + `strings_errors.xml` (Task 6) |
| States | Screen states inside component doc | `03_SEMANTIC_STATE_MODEL.md`: 8 screen states, 9 tool-availability states, 6 result states, 5 feedback levels | Availability model maps onto `ToolResolution`/`BlockedReason` (Task 6) |
| Screens | 10 per-screen docs | 5 consolidated docs | Same coverage, grouped: Home/Tools, Inspectors, Diagnostics/Reports, Net/Sensor/BT, AI/Settings |
| Journey | UI-0..UI-6 | V5-0..V5-6 | Same stages; this plan's Tasks 0–11 implement V5-0..V5-6 |
| Assets | Same 2 reference PNGs | Same files (renamed: `logo_candidates.png`, `splash_reference.png`) | Identical bytes; production-asset rules unchanged (Task 1) |
| Compose guide | `22_COMPOSE_MAPPING_GUIDE.md` | `20_ADAPTIVE_COMPOSE.md` adds width classes (compact/medium/expanded) | Adaptive nav is new work (Task 7) |

---

### Task 0: V5-0 — UI audit

**Files:**
- Create: `docs/04_audits/V5_UI_AUDIT.md`

**Interfaces:**
- Produces: the per-feature "files to change" lists consumed by Tasks 7–10; conflict log consumed by Task 2 (token naming) and Task 6 (error sources).

- [ ] **Step 1: Inventory theme/components** — list every file under `core/ui/src/main`, every composable in `feature/*/src/main` (name + file), and every `MaterialTheme.colorScheme`/hard-coded `Color(0x…)`/`dp` literal in feature code:

```bash
grep -rn "Color(0x\|\.dp\b" feature/*/src/main --include="*.kt" | wc -l
find feature -name "*.kt" -path "*src*" | sort
```

- [ ] **Step 2: Map states and errors** — for each feature screen: which `UiState`s exist, how permission/blocked states render today, and every `catch` that surfaces text to UI:

```bash
grep -rn "catch\s*(" feature/*/src/main core/*/src/main --include="*.kt"
grep -rn "UiState\|sealed interface.*State" feature/*/src/main --include="*.kt" -l
```

- [ ] **Step 3: Write `docs/04_audits/V5_UI_AUDIT.md`** — sections per feature module: existing screens/composables, states handled/missing vs v5 `03_SEMANTIC_STATE_MODEL.md`, error surfaces needing the friendly mapper, duplicate/one-off styling to delete, v4/v5/code conflicts (classify per `PLAN_MIGRATION_LOG.md` protocol; breaking scope conflicts need owner approval, logged in `docs/02_foundations/PLAN_MIGRATION_LOG.md`), reusable code worth promoting to `:core:ui`, explicit file-change list for Tasks 7–10.

- [ ] **Step 4: Commit**

```bash
git add docs/04_audits/V5_UI_AUDIT.md
git commit -m "docs: v5 UI audit — inventory, state/error gaps, migration file lists"
git push origin
```

---

### Task 1: V5-1a — Production brand assets + system splash

**Files:**
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/drawable/ic_launcher_monochrome.xml`, `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`, `app/src/main/res/values/ic_launcher_background.xml`, `app/src/main/res/values/themes.xml` (add splash theme)
- Modify: `app/src/main/AndroidManifest.xml` (theme + `android:icon`), `gradle/libs.versions.toml` + `app/build.gradle.kts` (`core-splashscreen`), `app/src/main/java/io/github/droidkit/MainActivity.kt` (`installSplashScreen()`)
- Delete: generated `mipmap-*` raster launcher assets once vectors verified (check `git status` — keep any still referenced)

**Interfaces:**
- Consumes: `docs/03_design_system/branding/droidkit-mark.svg` geometry (already encoded in `core/ui/.../theme/Brand.kt` `DroidKitMark`)
- Produces: production adaptive icon + branded cold-start splash; no API for later tasks

- [ ] **Step 1: Foreground/monochrome VectorDrawables** — 108dp viewport, mark centered with safe-zone padding (~27dp inset → 54dp glyph):

```xml
<!-- app/src/main/res/drawable/ic_launcher_foreground.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <group android:scaleX="0.5" android:scaleY="0.5"
        android:translateX="27" android:translateY="27">
        <path android:strokeColor="#3CE88A" android:strokeWidth="14"
            android:strokeLineCap="round" android:strokeLineJoin="round"
            android:pathData="M54 82 L27 66.5 L27 42 L52.5 26 L56.5 28" />
        <path android:strokeColor="#3CE88A" android:strokeWidth="14"
            android:strokeLineCap="round" android:strokeLineJoin="round"
            android:pathData="M74.5 37.6 L81.5 42 L81.5 65.5 L64.6 75.6" />
        <path android:fillColor="#86F595"
            android:pathData="M46.5 42.5 Q46.5 35 54 35 Q61.5 35 61.5 42.5 L61.5 65.5 Q61.5 73 54 73 Q46.5 73 46.5 65.5 Z" />
    </group>
</vector>
```

Note: solid brand colors (no `<aapt:attr>` gradient) so the same file works as `android:monochrome` source (Android tints it). Copy to `ic_launcher_monochrome.xml` with both paths in `android:fillColor="#FFFFFFFF"`/`strokeColor="#FFFFFFFF"` white.

- [ ] **Step 2: Background + adaptive icon**

```xml
<!-- app/src/main/res/values/ic_launcher_background.xml -->
<resources><color name="ic_launcher_background">#0C1416</color></resources>

<!-- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml (+ ic_launcher_round.xml identical) -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

- [ ] **Step 3: Splash** — add to `gradle/libs.versions.toml`: `core-splashscreen = { module = "androidx.core:core-splashscreen", version = "1.0.1" }` and `implementation(libs.core.splashscreen)` in `app/build.gradle.kts`. Theme:

```xml
<!-- app/src/main/res/values/themes.xml -->
<style name="Theme.DroidKit.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#0C1416</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.DroidKit</item>
</style>
```

Manifest `android:theme="@style/Theme.DroidKit.Splash"`; in `MainActivity.onCreate` call `installSplashScreen()` before `super.onCreate`. No artificial delay — if init continues, route to the existing in-app loading state per `23_ASSET_RULES.md`.

- [ ] **Step 4: Validate + commit**

```bash
./gradlew :app:assembleDebug
git add app/src/main gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat(ui): production adaptive icon, monochrome icon, branded splash"
git push origin
```

---

### Task 2: V5-1b — Semantic tokens + theme

**Files:**
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/theme/Spacing.kt`, `Shape.kt`, `Type.kt`
- Modify: `core/ui/src/main/java/io/github/droidkit/core/ui/theme/Theme.kt`, create `core/ui/src/main/java/io/github/droidkit/core/ui/theme/SemanticColors.kt`
- Modify: `docs/02_foundations/j03_design/J03_DESIGN_TOKENS.md` (append v5-name mapping table)

**Interfaces:**
- Produces (consumed by Tasks 3–10):
  - `DroidKitSemanticColors` + `LocalDroidKitColors`; access via `DroidKitTheme.colors` / `MaterialTheme.droidKitColors`
  - `DroidKitSpacing` + `LocalDroidKitSpacing`; access via `DroidKitTheme.spacing`
  - `DroidKitExtraTypography` + `LocalDroidKitTypography` (`brandDisplay`, `technicalData`)
  - `DroidKitTheme(dynamicColor: Boolean = false)` — dynamic off by default (v5 brand identity decision; record in audit doc if overridden)

- [ ] **Step 1: `SemanticColors.kt`**

```kotlin
package io.github.droidkit.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable

@Immutable
data class DroidKitSemanticColors(
    val brandPrimary: Color, val brandSecondary: Color,
    val brandGradientStart: Color, val brandGradientEnd: Color, val brandGlow: Color,
    val background: Color, val surface: Color, val surfaceVariant: Color,
    val surfaceElevated: Color, val outline: Color, val divider: Color,
    val textPrimary: Color, val textSecondary: Color, val textDisabled: Color,
    val onBrand: Color,
    val statusSuccess: Color, val statusWarning: Color, val statusError: Color,
    val statusInfo: Color, val statusUnsupported: Color, val statusSetupRequired: Color,
)

val DarkDroidKitColors = DroidKitSemanticColors(
    brandPrimary = DroidKitBrand.Green, brandSecondary = DroidKitBrand.Teal,
    brandGradientStart = DroidKitBrand.Green, brandGradientEnd = DroidKitBrand.Teal,
    brandGlow = DroidKitBrand.Green.copy(alpha = 0.35f),
    background = DroidKitBrand.Ink, surface = Color(0xFF121B1D),
    surfaceVariant = Color(0xFF1A2528), surfaceElevated = Color(0xFF223033),
    outline = Color(0xFF2E3E42), divider = Color(0xFF243236),
    textPrimary = Color(0xFFE8F0EF), textSecondary = Color(0xFF9DB3B0),
    textDisabled = Color(0xFF5A6E6C), onBrand = DroidKitBrand.Ink,
    statusSuccess = Color(0xFF3CE88A), statusWarning = Color(0xFFF5C04E),
    statusError = Color(0xFFFF7A7A), statusInfo = Color(0xFF5AC8DC),
    statusUnsupported = Color(0xFF93A4AB), statusSetupRequired = Color(0xFFE8A54B),
)

val LightDroidKitColors = DarkDroidKitColors.copy(
    brandPrimary = DroidKitBrand.GreenOnLight, brandSecondary = Color(0xFF0E7C8C),
    brandGlow = DroidKitBrand.GreenOnLight.copy(alpha = 0.25f),
    background = Color(0xFFF7FAF9), surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9F0EE), surfaceElevated = Color(0xFFFFFFFF),
    outline = Color(0xFFC3D2CF), divider = Color(0xFFD9E4E1),
    textPrimary = Color(0xFF12201E), textSecondary = Color(0xFF4A5F5C),
    textDisabled = Color(0xFF9AAEAB), onBrand = Color(0xFFFFFFFF),
    statusSuccess = Color(0xFF0E9E6B), statusWarning = Color(0xFF9A6B00),
    statusError = Color(0xFFB3261E), statusInfo = Color(0xFF0E7C8C),
    statusUnsupported = Color(0xFF5F6E75), statusSetupRequired = Color(0xFFB26A00),
)

val LocalDroidKitColors = staticCompositionLocalOf { DarkDroidKitColors }
```

- [ ] **Step 2: `Spacing.kt`** — merges J03 ramp with the v5 4–48dp ramp:

```kotlin
package io.github.droidkit.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class DroidKitSpacing(
    val xs: Dp = 4.dp, val sm: Dp = 8.dp, val md: Dp = 12.dp,
    val lg: Dp = 16.dp, val xl: Dp = 20.dp, val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp, val huge: Dp = 40.dp, val jumbo: Dp = 48.dp,
    val touchTarget: Dp = 48.dp, val listItem: Dp = 56.dp, val cardGap: Dp = 8.dp,
    val screenHorizontal: Dp = 16.dp,
)

val LocalDroidKitSpacing = staticCompositionLocalOf { DroidKitSpacing() }
```

- [ ] **Step 3: `Shape.kt`**

```kotlin
package io.github.droidkit.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DroidKitShapes = Shapes(
    small = RoundedCornerShape(8.dp),   // chips, badges, small buttons
    medium = RoundedCornerShape(12.dp), // cards, dialogs
    large = RoundedCornerShape(16.dp),  // sheets, prominent cards
    extraLarge = RoundedCornerShape(24.dp), // hero/bento surfaces
)
```

- [ ] **Step 4: `Type.kt`** — J03 type table into M3 `Typography` + two extras:

```kotlin
package io.github.droidkit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DroidKitTypography = Typography(
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.W600),  // ScreenTitle
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W600),    // SectionTitle
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500),     // card headers
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W400),      // Body
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W400),     // info rows
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W400),      // Supporting
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500),     // buttons
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W500),     // badges
)

@Immutable
data class DroidKitExtraTypography(
    val brandDisplay: TextStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.W700),
    val technicalData: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.W400,
    ),
)

val LocalDroidKitTypography = staticCompositionLocalOf { DroidKitExtraTypography() }
```

- [ ] **Step 5: Rewrite `Theme.kt`** — map semantic colors onto the M3 scheme, provide the locals:

```kotlin
@Composable
fun DroidKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dk = if (darkTheme) DarkDroidKitColors else LightDroidKitColors
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = dk.brandPrimary, secondary = dk.brandSecondary,
            background = dk.background, surface = dk.surface,
            surfaceVariant = dk.surfaceVariant, outline = dk.outline,
            onPrimary = dk.onBrand, onBackground = dk.textPrimary,
            onSurface = dk.textPrimary, onSurfaceVariant = dk.textSecondary,
            error = dk.statusError,
        )
        else -> lightColorScheme(
            primary = dk.brandPrimary, secondary = dk.brandSecondary,
            background = dk.background, surface = dk.surface,
            surfaceVariant = dk.surfaceVariant, outline = dk.outline,
            onPrimary = dk.onBrand, onBackground = dk.textPrimary,
            onSurface = dk.textPrimary, onSurfaceVariant = dk.textSecondary,
            error = dk.statusError,
        )
    }
    CompositionLocalProvider(
        LocalDroidKitColors provides dk,
        LocalDroidKitSpacing provides DroidKitSpacing(),
        LocalDroidKitTypography provides DroidKitExtraTypography(),
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = DroidKitTypography,
            shapes = DroidKitShapes, content = content)
    }
}

object DroidKitTheme {
    val colors: DroidKitSemanticColors @Composable get() = LocalDroidKitColors.current
    val spacing: DroidKitSpacing @Composable get() = LocalDroidKitSpacing.current
    val extraTypography: DroidKitExtraTypography @Composable get() = LocalDroidKitTypography.current
}
```

- [ ] **Step 6: Update `docs/02_foundations/j03_design/J03_DESIGN_TOKENS.md`** — append a "v5 semantic names" table mapping J03 tokens → `DroidKitSemanticColors`/`DroidKitSpacing`/extras (e.g. `spaceMd`→`spacing.md`, mono `bodyMedium`→`extraTypography.technicalData`). Doc sync only; J03 values stay valid.

- [ ] **Step 7: Validate + commit** — `:core:ui` compiles, then `:app:assembleDebug` (shared module):

```bash
./gradlew :core:ui:compileDebugKotlin :app:assembleDebug
git add core/ui docs/02_foundations/j03_design/J03_DESIGN_TOKENS.md
git commit -m "feat(ui): v5 semantic tokens — colors, spacing, shapes, typography, dark-first theme"
git push origin
```

---

### Task 3: V5-1c — Shared primitives and badges

**Files:**
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/components/Badges.kt`, `BentoCard.kt`, `TechnicalValue.kt`, `DiagnosticProgress.kt`
- Modify: `core/ui/src/main/java/io/github/droidkit/core/ui/InfoRow.kt` (route value rendering through `TechnicalValue` where mono applies — keep signature), `core/ui/src/main/java/io/github/droidkit/core/ui/components/Feedback.kt` (move badge types here only if audit shows no `StatusBadge` elsewhere; do not break imports)

**Interfaces:**
- Produces: `StatusBadge(state: TestResultState)`, `CapabilityBadge(text)`, `ResultBadge(state: TestResultState, count: Int)`, `BentoCard(span: BentoSpan, content)`, `TechnicalValue(value: String?, unavailableText = "Not exposed")`, `DiagnosticProgress(current: Int, total: Int, label: String?)` — names frozen for Tasks 7–10.

- [ ] **Step 1: `TechnicalValue.kt`** — mono value with honest empty path:

```kotlin
@Composable
fun TechnicalValue(value: String?, modifier: Modifier = Modifier, unavailable: String = "Not exposed") {
    Text(
        text = value ?: unavailable,
        style = DroidKitTheme.extraTypography.technicalData,
        color = if (value == null) DroidKitTheme.colors.textSecondary else DroidKitTheme.colors.textPrimary,
        modifier = modifier,
    )
}
```

- [ ] **Step 2: `Badges.kt`** — icon + label + color (never color-only):

```kotlin
@Composable
fun StatusBadge(state: TestResultState, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    val (icon, label, color) = when (state) {
        TestResultState.PASS -> Triple(Icons.Default.CheckCircle, "Passed", c.statusSuccess)
        TestResultState.FAIL -> Triple(Icons.Default.Cancel, "Failed", c.statusError)
        TestResultState.NOT_TESTED -> Triple(Icons.Default.RadioButtonUnchecked, "Not tested", c.textSecondary)
        TestResultState.NOT_SUPPORTED -> Triple(Icons.Default.Block, "Not supported", c.statusUnsupported)
        TestResultState.PERMISSION_REQUIRED -> Triple(Icons.Default.Key, "Permission required", c.statusSetupRequired)
        TestResultState.UNKNOWN -> Triple(Icons.Default.HelpOutline, "Unknown", c.statusInfo)
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small, modifier = modifier) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable fun CapabilityBadge(text: String) = /* same pill, statusInfo color */
@Composable fun ResultBadge(state: TestResultState, count: Int) = /* StatusBadge + " × $count" suffix */
```

- [ ] **Step 3: `BentoCard.kt`** — grid-span card with optional Aurora edge (brand accent only where `accent = true`):

```kotlin
enum class BentoSpan { FULL, HALF, THIRD }

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
// Implementation: MaterialTheme.shapes.large surface; when accent, 1dp border
// with Brush.linearGradient(listOf(brandGradientStart, brandGradientEnd)) at low alpha.
// onClick != null → clickable + role Button + mergeDescendants.
```

- [ ] **Step 4: `DiagnosticProgress.kt`** — linear progress + "Test X of Y" + current label, `progressbar` semantics with explicit `contentDescription`.

- [ ] **Step 5: Validate + commit** (`:core:ui` compile + `:app:assembleDebug`, screenshot-preview one badge in both themes)

---

### Task 4: V5-1d — Shared screen states + a11y helpers

**Files:**
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/components/ScreenStates.kt`, `core/ui/src/main/java/io/github/droidkit/core/ui/semantics/Semantics.kt`
- Retire: fold `Feedback.kt`'s `EmptyState`/`BlockedReasonList` into `ScreenStates.kt` (keep thin deprecated delegates one release if imports are widespread — audit decides)

**Interfaces:**
- Produces: `LoadingState`, `EmptyState(title, body?, action?)`, `PermissionRequiredState(permission, rationale, onRequest, onOpenSettings)`, `SetupRequiredState(provider, instructions, onSetup)`, `UnsupportedState(reason)`, `OfflineState(onRetry?)`, `FriendlyErrorState(error: FriendlyError, onAction)` — the v5 8-state model; `Modifier.statusSemantics(state)` + `Modifier.heading()` helpers.

- [ ] **Step 1: `ScreenStates.kt`** — one internal `StateScaffold(icon, title, body, primaryAction?, secondaryAction?)` composable all seven states delegate to; every state gets `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` where announcements matter and a labeled CTA per state. `PermissionRequiredState` covers both normal and permanently-denied (`openSettings` CTA when denied permanently — per error catalog).

- [ ] **Step 2: `Semantics.kt`**

```kotlin
fun Modifier.statusSemantics(state: TestResultState): Modifier = semantics {
    contentDescription = when (state) {
        TestResultState.PASS -> "Passed"; TestResultState.FAIL -> "Failed"
        TestResultState.NOT_TESTED -> "Not tested"; TestResultState.NOT_SUPPORTED -> "Not supported"
        TestResultState.PERMISSION_REQUIRED -> "Permission required"; TestResultState.UNKNOWN -> "Unknown"
    }
}

fun Modifier.heading(): Modifier = semantics { heading() }

fun Modifier.mergeAsSingleControl(description: String): Modifier =
    semantics(mergeDescendants = true) { contentDescription = description }
```

- [ ] **Step 3: Validate + commit** (`:app:assembleDebug` — shared module)

---

### Task 5: V5-1e — Risk and AI components

**Files:**
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/components/Risk.kt` (`RiskBadge`, `ConsequencePanel`, `ConfirmationSheet`), `AiInsight.kt` (`AIInsightCard`)
- Modify: `feature/nfc` write/erase flows + any `SafetyLevel >= SYSTEM_CHANGE` call sites from the Task 0 audit to route through `ConfirmationSheet` (only where a bespoke dialog duplicates it — record swaps in audit doc)

**Interfaces:**
- Produces: `RiskBadge(level: SafetyLevel)`, `ConsequencePanel(target: String, consequence: String, reversible: Boolean)`, `ConfirmationSheet(title, target, consequence, confirmLabel: String, requireTypedTarget: Boolean, onConfirm, onDismiss)`, `AIInsightCard(title, body, onDismiss?)`. Risk levels 0–4 scale confirmation strength per `00_specs/playbook/24`; level ≥3 uses `requireTypedTarget` or delayed-enable confirm. `AIInsightCard` = subtle Aurora/Glass identity surface, always labeled "AI" — never ambiguous with measured data.

- [ ] **Step 1: `Risk.kt`** — `ConsequencePanel` (target, consequence, reversibility row; `statusWarning`/`statusError` accent by level) + `ConfirmationSheet` (`ModalBottomSheet`, exact-verb `confirmLabel` e.g. "Write Tag"/"Uninstall" — never "Continue"; confirm button is last in focus order; `requireTypedTarget` gates enable on `TextField` match).

- [ ] **Step 2: `AiInsightCard.kt`** — `Surface` with 1dp gradient border + `Icon(Icons.Default.AutoAwesome)` + `Text("AI", labelSmall badge)` header slot, `body` slot, optional dismiss; content description prefix "AI insight: ".

- [ ] **Step 3: Validate + commit**

---

### Task 6: V5-2 — Availability presentation + friendly-error system

**Files:**
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/availability/ToolAvailability.kt`, `core/ui/src/main/java/io/github/droidkit/core/ui/errors/FriendlyError.kt`, `core/ui/src/main/java/io/github/droidkit/core/ui/errors/FriendlyErrorMapper.kt`, `core/ui/src/main/res/values/strings_errors.xml`
- Modify: `core/ui/src/main/java/io/github/droidkit/core/ui/components/ToolCard.kt` (use `ToolAvailability` for badge/state), `Feedback.kt` `BlockedReason.describe()` replaced by mapper output (keep delegates until call sites migrate)

**Interfaces:**
- Consumes: `ToolResolution`, `BlockedReason`, `ProviderStatus` (`:core:privileges`)
- Produces:
  - `enum class ToolAvailability { Ready, PermissionRequired, SetupRequired, HardwareUnavailable, UnsupportedAndroidVersion, ProviderUnavailable, Offline, TemporarilyUnavailable, Error }`
  - `fun ToolResolution.toAvailability(): ToolAvailability` + `fun ToolAvailability.label(): String` + `@Composable fun ToolAvailability.badgeColor(): Color`
  - `data class FriendlyError(title, body, impact?, primary: ErrorAction?, secondary: ErrorAction?, technicalDetails?, severity: ErrorSeverity, recoverable: Boolean)`; `enum class ErrorAction { RETRY, OPEN_SETTINGS, ALLOW_PERMISSION, RECONNECT, CONTINUE_WITHOUT_AI, TRY_LATER, SETUP_GUIDE, DISMISS }`; `enum class ErrorSeverity { INFO, WARNING, ERROR, CRITICAL }`
  - `fun Throwable.toFriendlyError(context: ErrorContext = ErrorContext.GENERIC): FriendlyError` — features may map domain error types before calling; raw `message` only ever lands in `technicalDetails`, never `body`.

- [ ] **Step 1: `strings_errors.xml`** — encode v5 `06_ERROR_CATALOG.md` verbatim as resources (one `<string>` per catalog line, keys like `error_permission_mic_title`, `error_ai_auth_title`, `error_shizuku_not_running_body`; `Not supported`, `Permission required`, `Unavailable`, `Not reported by this device` common strings). This is the localization-ready string base for Tasks 7–10.

- [ ] **Step 2: `ToolAvailability.kt`** — total mapping; add `Offline`/`TemporarilyUnavailable` cases mapping from `ProviderStatus.Unavailable(reason)` where reason text matches offline/temporary, else `ProviderUnavailable`:

```kotlin
fun ToolResolution.toAvailability(): ToolAvailability = when (this) {
    ToolResolution.Ready -> ToolAvailability.Ready
    is ToolResolution.Blocked -> reasons.first().toAvailability()
}

fun BlockedReason.toAvailability(): ToolAvailability = when (this) {
    is BlockedReason.MissingPermission -> ToolAvailability.PermissionRequired
    is BlockedReason.MissingHardware -> ToolAvailability.HardwareUnavailable
    is BlockedReason.RequiresApi -> ToolAvailability.UnsupportedAndroidVersion
    is BlockedReason.NeedsProvider -> when (status) {
        is ProviderStatus.NeedsSetup -> ToolAvailability.SetupRequired
        is ProviderStatus.Unavailable -> ToolAvailability.ProviderUnavailable
        ProviderStatus.Ready -> ToolAvailability.Ready
    }
}
```

- [ ] **Step 3: `FriendlyError.kt` + `FriendlyErrorMapper.kt`** — map by error class + `ErrorContext` (GENERIC/USB/NETWORK/AI/SHIZUKU/ADB/NFC): `SecurityException`→permission strings; `IOException` on USB→"USB inspection stopped…"; AI codes (NO_KEY/AUTH_ERROR/OFFLINE/RATE_LIMITED/TIMEOUT/PROVIDER_ERROR/MALFORMED_RESPONSE)→catalog strings; Shizuku/ADB→respective catalog entries; unknown→generic calm fallback + `technicalDetails` = `${e::class.simpleName}: sanitized message` (strip tokens/keys via regex `Bearer \S+|key=\S+` before storing).

- [ ] **Step 4: `FriendlyErrorState`** renders title/body/impact + action buttons mapped from `ErrorAction` + expandable "Technical details" section (`technicalDetails`).

- [ ] **Step 5: Validate + commit** — unit test the mapper (`core/ui` test or move mapper to `:core:common` test if android deps force it; at minimum a JVM test over `toAvailability`):

```bash
./gradlew :core:ui:testDebugUnitTest :app:assembleDebug
git add core/ui
git commit -m "feat(ui): tool availability model + friendly error mapper and state"
git push origin
```

---

### Task 7: V5-3 — Adaptive shell: Home bento + Tools + navigation

**Files:**
- Modify: `app/src/main/java/io/github/droidkit/MainActivity.kt` (or wherever the root scaffold lives — audit Step 1 names it), add `app/.../DroidKitNavHost.kt` width-class branch
- Create: `app/src/main/java/io/github/droidkit/navigation/AdaptiveNavigation.kt`
- Modify: `feature/dashboard/...` Home screen → bento per `screens/10_HOME_TOOLS.md`; `feature/tools/...` → minimal list per same doc

**Interfaces:**
- Consumes: `BentoCard`, badges, states from Tasks 3–4; `calculateWindowSizeClass` (add `androidx.compose.material3:material3-window-size-class` to catalog if absent).
- Produces: `DroidKitAdaptiveNav(content)` — `NavigationBar` at compact, `NavigationRail` + content row at medium/expanded; `ListDetailPaneScaffold` left to per-feature tasks (8–10) where useful.

- [ ] **Step 1: `AdaptiveNavigation.kt`** — `WindowWidthSizeClass.Compact` → `Scaffold(bottomBar = { NavigationBar … })`; else `Row { NavigationRail … ; content() }` with rail items mirroring bottom-nav destinations; rail expanded state gets `NavigationRailItem` labels always-visible per M3.

- [ ] **Step 2: Home** — hierarchy exactly per `10_HOME_TOOLS.md`: compact device summary `BentoCard` (FULL) → Full Diagnostic hero action → actionable capability notice → favorites/quick tools (HALF/THIRD `BentoCard`s) → privacy-safe recent activity. No wall of cards; Aurora accent on the hero only.

- [ ] **Step 3: Tools** — search + filter chips over `ToolRegistry`; `ToolCard` rows show availability via `ToolAvailability` badge; blocked tools stay discoverable with the reason line; no glass.

- [ ] **Step 4: Validate + commit** — `:app:assembleDebug`, verify compact + one wider width in preview/emulator.

---

### Task 8: V5-4a — Inspectors migration (Device / Apps / USB / NFC)

**Files:** per Task 0 audit lists, expected:
- `feature/deviceinfo/**` — `screens/11_INSPECTORS.md` Device: tab groups Overview/System/Hardware/Memory-Storage/Display/Battery/Capabilities; dense `InfoRow`s; copy actions; `Unavailable` explicit; AI Explain stays secondary.
- `feature/apps/**` — compact list→detail; Overview/Permissions/Activities/Services/Receivers/Providers/APK/Actions; elevated actions visually separated (`surfaceVariant` group + `RiskBadge`); list-detail layout at expanded width.
- `feature/usb/**` — Host status → device → identity/classification → interfaces/endpoints/descriptors via `TechnicalValue`; permission/disconnect → `PermissionRequiredState`/`FriendlyErrorState`; ChipGenius honesty note stays.
- `feature/nfc/**` — State → Scan → Tag → Techs → NDEF → Records; writer = preview → `ConfirmationSheet` → result; Tag Doctor uses PASS/WARNING/UNSUPPORTED badges.

**Steps (repeat per module — audit names files):**
- [ ] Replace bespoke cards/rows with `SectionCard`/`InfoRow`/`TechnicalValue`; delete local color/dp literals for token equivalents.
- [ ] Route every blocked/empty/error branch through Task 4 state components and `BlockedReason → ToolAvailability` labels; never color-only.
- [ ] Add `statusSemantics`/`mergeAsSingleControl` where audit flagged missing descriptions; associate error text with its control.
- [ ] Mutating flows → `ConfirmationSheet` with exact-verb labels.
- [ ] Slice DoD (Task 11 checklist) + `:app:assembleDebug` + commit per module.

### Task 9: V5-4b — Diagnostics, Reports, Network/Bluetooth/Sensors

**Files:** audit lists for `feature/diagnostics`, `feature/reports`, `feature/network`, `feature/bluetooth`, `feature/sensors`, lab features (`display`,`audio`,`camera`,`battery`,`appops`).

- [ ] Phone Doctor: `DiagnosticProgress` + instruction card → user result controls → summary; summary separately counts Passed/Failed/Skipped/Unsupported/Permission Required (`ResultBadge` per state); manual tests never auto-verdict.
- [ ] Reports: professional minimal; history → detail → export; AI summary optional and visually separated (`AIInsightCard`); delete → `ConfirmationSheet`; export preview flags sensitive categories.
- [ ] Network: connection/DNS/reachability as independent layers — never one fake health score.
- [ ] Bluetooth: explicit time-bounded scans; empty result → `EmptyState`, not error.
- [ ] Sensors: value + unit + accessible text; changing value ≠ calibrated/healthy (no auto-good-state).
- [ ] Slice DoD + build + commit per module.

### Task 10: V5-5 — Advanced/AI surfaces

**Files:** audit lists for `feature/permissions` (Permission Center), `feature/insights` (AI), `feature/settings`, capability/provider screens, `feature/appops`.

- [ ] Capability Center: Standard/Shizuku/Wireless ADB/Root(future) rows — status + capabilities + setup + limitations; `SetupRequiredState`/`UnsupportedState` per provider truth; never hard-code availability.
- [ ] AI surfaces: `AIInsightCard` for generated content only; AI settings solid/readable (Aurora/Glass identifies insight surfaces, not config); no-key/offline/auth/rate-limit/timeout/provider errors via `FriendlyErrorState`; never show full key.
- [ ] Permission Center: grouped M3 list; permanently-denied → Open Settings CTA.
- [ ] Settings: simple grouped Material lists — no Bento/glass.
- [ ] Slice DoD + build + commit.

---

### Task 11: V5-6 — QA matrix + tracker close-out

- [ ] Run the matrix (preview + emulator where available; mark device-bound items `BLOCKED: hardware` in the audit doc, not `[x]`):
  - [ ] dark + light themes
  - [ ] compact / medium / expanded widths
  - [ ] portrait + landscape
  - [ ] large font + display scale (core actions don't clip; rows may stack)
  - [ ] TalkBack on critical flows (diagnostic run, NFC write confirm, permission prompts)
  - [ ] keyboard/focus on larger width
  - [ ] permission / setup / unsupported / error / offline states per screen
  - [ ] AI-disabled and AI-failure paths
  - [ ] consistency screenshot review
- [ ] Per-slice DoD — every migrated screen passes v5 `22_DEFINITION_OF_DONE.md`:
  - [ ] v5 design language applied appropriately (Bento on overviews only; Aurora = brand moments; glass rare)
  - [ ] semantic tokens/shared components used; no feature-local literals
  - [ ] state from ViewModel/domain contracts; no hard-coded availability
  - [ ] loading/empty/permission/setup/unsupported/offline/error states handled
  - [ ] errors friendly + actionable; technical details sanitized + expandable
  - [ ] unsupported/permission/unknown never mislabeled FAIL
  - [ ] TalkBack semantics meaningful; status not color-only; large text works
  - [ ] dark/light + adaptive width reviewed
  - [ ] destructive actions show target/consequence/exact-verb confirmation
  - [ ] AI content distinct from measured data; no secrets displayed/logged
  - [ ] changed files identified; targeted validation run; no unrelated rewrites
- [ ] Update `01_plans/10_DroidKit_Master_Journey_Checklist.md` → "UI/UX V5 Design System Pass" section; tick feature DoD line "UI follows the design system" where the migration landed.
- [ ] Final `:app:assembleDebug` + full changed-scope unit tests; commit tracker/doc updates.

## Self-Review Notes

- Spec coverage: V5-0→T0, V5-1→T1–T5, V5-2→T6, V5-3→T7, V5-4→T8–T9, V5-5→T10, V5-6→T11. All v5 docs referenced.
- Known deliberate gaps: exact per-file edits in Tasks 8–10 are produced by Task 0's audit (cannot know them before inventorying); `Offline`/`TemporarilyUnavailable` availability states approximate onto existing `BlockedReason` — if the audit shows real offline detection exists, extend `BlockedReason` rather than inventing parallel state (log the change in `PLAN_MIGRATION_LOG.md`).
