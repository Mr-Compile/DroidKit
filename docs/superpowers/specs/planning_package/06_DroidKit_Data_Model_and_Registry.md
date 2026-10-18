# DroidKit - Data Model and Tool Registry

## Room Entities

### DiagnosticSession
- id
- startedAt
- completedAt
- deviceSnapshotId
- overallStatus
- notes
- appVersion

### DiagnosticResult
- id
- sessionId
- tool/test ID
- category
- result status
- measured data JSON/structured fields as appropriate
- user note
- timestamp

### DeviceSnapshot
- id
- capturedAt
- manufacturer
- model
- device/build identifiers that are appropriate to store
- Android version/API
- security patch if exposed
- ABI
- memory summary
- storage summary
- display summary
- capability/hardware summary

### SavedReport
- id
- sessionId
- generatedAt
- report format
- local URI/path reference managed safely

## DataStore
- onboardingCompleted
- theme
- favorites
- recent tool preferences
- advanced mode preference
- Shizuku/ADB setup hints/state that is safe to persist
- report preferences

## Tool Definition Concept
ToolDefinition:
- id: String
- name: String
- description: String
- category: ToolCategory
- route: String
- minApi: Int
- hardwareRequirements: Set<HardwareCapability>
- permissionRequirements: Set<AppPermission>
- capabilityRequirements: Set<Capability>
- riskLevel: RiskLevel
- keywords: Set<String>

## Capability Status
Examples:
- Available(provider)
- PermissionRequired(permission)
- SetupRequired(provider options)
- HardwareUnavailable
- UnsupportedApi
- TemporarilyUnavailable(reason)
- Error(reason)

## Diagnostic Result Status
- PASS
- FAIL
- NOT_TESTED
- NOT_SUPPORTED
- PERMISSION_REQUIRED
- UNKNOWN

## Important Privacy Principle
Do not collect persistent identifiers simply because they are available. Store only information needed for a diagnostic/report feature, and clearly communicate what is included when a report is exported.
