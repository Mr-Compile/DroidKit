# UI/UX Implementation Journey

## UI-0 Audit
- [ ] Inventory existing screens/components/theme.
- [ ] Map routes to ToolRegistry.
- [ ] Map UiState/capability/permission states.
- [ ] Identify duplicate styling.
- [ ] Record conflicts with v4.

## UI-1 Foundation
- [ ] Brand assets integrated.
- [ ] System splash implemented correctly.
- [ ] Light/dark semantic tokens.
- [ ] Typography/spacing/shape.
- [ ] Core scaffold/navigation.
- [ ] Accessibility semantics helpers.
- [ ] Shared state components.

## UI-2 Core shell
- [ ] Home
- [ ] Tools/search
- [ ] Diagnose
- [ ] Settings
- [ ] adaptive nav

## UI-3 Inspectors
- [ ] Device
- [ ] Apps
- [ ] USB
- [ ] NFC
- [ ] Network/Bluetooth/Sensors

## UI-4 Diagnostics/Reports
- [ ] guided tests
- [ ] progress/result controls
- [ ] summary
- [ ] history/report detail/export

## UI-5 Advanced/AI
- [ ] Capability Center
- [ ] Permission Center
- [ ] AI settings
- [ ] AI insight components
- [ ] risk confirmation system

## UI-6 QA
- [ ] compact/medium/expanded
- [ ] portrait/landscape
- [ ] light/dark
- [ ] large font/display scale
- [ ] TalkBack critical paths
- [ ] keyboard/focus
- [ ] missing permission/hardware
- [ ] offline/AI unavailable
- [ ] screenshot review for consistency

Do not redesign every feature in one commit. Migrate by shared foundation, then screen groups.
