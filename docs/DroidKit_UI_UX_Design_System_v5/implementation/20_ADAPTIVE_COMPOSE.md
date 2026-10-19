# Adaptive Compose Plan
Compact: bottom nav, single column, stack technical rows when needed.
Medium: optional rail, readable two-column tools, balanced inspector groups.
Expanded: rail, list-detail for Apps/USB/Reports, persistent detail when useful.

FeatureRoute observes ViewModel/navigation callbacks.
FeatureScreen is state-driven.
ViewModel owns orchestration.
Domain/Repositories own logic.
CapabilityManager/PermissionManager own runtime truth.

Domain/System Error -> Mapper -> semantic presentation -> shared component.
User strings use Android resource/localization strategy.
