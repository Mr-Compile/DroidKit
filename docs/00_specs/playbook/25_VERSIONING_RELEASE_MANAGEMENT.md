# Versioning and Release Management

Use Semantic Versioning.

## Development map
0.1.0 Foundation
0.2.0 Device Inspector
0.3.0 Phone Doctor Core
0.4.0 Hardware Diagnostics
0.5.0 Reports
0.6.0 App Inspector
0.7.0 USB Lab
0.8.0 NFC + Network
0.9.0-alpha.N Integrated Alpha
1.0.0-beta.N Beta
1.0.0-rc.N Release Candidate
1.0.0 Stable

Post-v1 examples are roadmap suggestions, not promises.

## Android versioning
- versionName = human SemVer
- versionCode = monotonically increasing integer
Do not depend on a restrictive formula.

## Git
Recommended lightweight branches:
main
feature/<name>
fix/<name>
refactor/<name>
docs/<name>

Tag released milestones:
v0.1.0, v0.2.0, ..., v1.0.0

## CHANGELOG categories
Added
Changed
Fixed
Deprecated
Removed
Security
Known limitations

## Release checklist
- [ ] Scope frozen
- [ ] Changed files identified
- [ ] Required tests passed
- [ ] Release build compiled
- [ ] Install/upgrade verified
- [ ] versionName/versionCode correct
- [ ] CHANGELOG
- [ ] release notes
- [ ] Git tag
- [ ] artifact archived/published
