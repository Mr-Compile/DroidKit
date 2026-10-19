# Testing & QA Plan

## Validation rule
Identify changed files/features first and scope validation to those changes. Do not automatically run the entire test suite or broad static analysis unless changes affect shared/core areas or broader integration validation is specifically justified.

## Test categories
### Navigation
- Settings destinations open correctly
- Back/system gesture works
- No route duplication/loops
- State/scroll preservation where supported

### Search
- Relevant results
- No hidden/dev routes
- Empty/no-result behavior

### Favorites/Recents
- Persistence
- Deduplication
- Removal/stale IDs

### Permissions
- Granted/denied/permanently denied
- Android-version differences
- Return from system Settings

### Online/AI
- Online
- Offline
- Timeout/failure
- Unconfigured service
- No secret leakage

### UI
- Light/dark themes
- Small and large screens supported by project
- Large font scaling
- Long localized/text values where applicable
- No clipping/overflow

### Accessibility
- Semantics
- Focus
- Touch targets
- State announcements where appropriate

### Regression
Test directly affected existing flows and shared components.

## Final matrix
For each requirement report:
PASS / PARTIAL / NOT IMPLEMENTED / NOT APPLICABLE

Fix safe PARTIAL/NOT IMPLEMENTED items before final reporting.
