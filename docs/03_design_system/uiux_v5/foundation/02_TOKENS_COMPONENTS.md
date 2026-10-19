# Tokens & Components
Use semantic tokens rather than one-off styling.

## Colors
brandPrimary, brandSecondary, brandGradientStart, brandGradientEnd, brandGlow;
background, surface, surfaceVariant, surfaceElevated, outline, divider;
textPrimary, textSecondary, textDisabled, onBrand;
statusSuccess, statusWarning, statusError, statusInfo, statusUnsupported, statusSetupRequired.

Status always uses wording/icon plus color.

## Spacing
4/8/12/16/20/24/32/40/48 dp. Screen horizontal padding 16-24dp by width.

## Shapes
8 small, 12 medium, 16 large, 20-24 hero. Pills mainly for badges/chips.

## Typography
Brand/Display, ScreenTitle, SectionTitle, Body, Supporting, Label, TechnicalData, MonospaceTechnical.

## Shared Compose components
DroidKitScaffold, TopBar, BottomNavigation, NavigationRail, Section, Card, BentoCard, InfoRow, TechnicalValue, StatusBadge, CapabilityBadge, ToolCard, ToolRow, LoadingState, EmptyState, PermissionRequiredState, SetupRequiredState, UnsupportedState, OfflineState, FriendlyErrorState, DiagnosticProgress, ResultBadge, RiskBadge, ConsequencePanel, ConfirmationSheet, AIInsightCard.

Shared UI never calls repositories/system APIs/Groq/Shizuku/ADB directly.
