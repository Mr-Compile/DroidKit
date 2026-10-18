# Journey 17 - Maintenance

## Bug workflow
REPORT -> REPRODUCE -> CLASSIFY -> ROOT CAUSE -> FIX -> TARGETED VALIDATION -> REGRESSION IF NEEDED -> CHANGELOG -> PATCH RELEASE

## Severity
P0: data loss/security/major crash affecting broad use
P1: core feature unusable
P2: significant but workaround exists
P3: minor/cosmetic

## Patch checklist
- [ ] Reproduction documented
- [ ] Affected scope identified
- [ ] Smallest safe fix
- [ ] Targeted validation
- [ ] Regression scope justified
- [ ] CHANGELOG
- [ ] versionCode/versionName
- [ ] release artifact/tag

Minor releases add backward-compatible functionality. Major releases are reserved for substantial product/contracts changes.
