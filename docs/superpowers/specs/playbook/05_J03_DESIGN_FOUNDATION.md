# Journey 03 - Design Foundation

## UX principles
- Technical but understandable.
- Information density without clutter.
- No unexplained disabled controls.
- Every unavailable tool explains why.
- Dangerous actions describe exact consequences.
- Diagnostics give clear instructions and human-verifiable results.
- Light and dark themes.
- Accessibility from the start.

## Navigation
Bottom navigation:
Home | Tools | Diagnose | Settings

### Home
Device summary, capability status, quick tools, recent tools, diagnostic CTA.

### Tools
Search, categories, favorites, availability badges.

### Diagnose
Full diagnostic, quick tests, history, reports.

### Settings
Appearance, permissions/setup, advanced access, safety, data/reports, about.

## Design tokens to define
- [ ] Typography
- [ ] Spacing
- [ ] Shape/radius
- [ ] Elevation/surfaces
- [ ] Semantic colors/tokens
- [ ] Icon rules
- [ ] Motion rules
- [ ] Light theme
- [ ] Dark theme

## Core reusable components
- [ ] App scaffold
- [ ] Screen header
- [ ] Tool card
- [ ] Status badge
- [ ] Information row
- [ ] Section card
- [ ] Permission prompt
- [ ] Unsupported state
- [ ] Empty state
- [ ] Error state
- [ ] Loading state
- [ ] Risk confirmation dialog
- [ ] Diagnostic instruction card
- [ ] Pass/Fail/Skip controls
- [ ] Search field
- [ ] Filter chips

## Screen specification checklist
- [ ] Onboarding
- [ ] Home
- [ ] Tools/search/category
- [ ] Device Inspector
- [ ] Diagnostic start/test/summary/history
- [ ] Report detail/export
- [ ] Apps list/detail/components/permissions
- [ ] USB list/detail
- [ ] NFC scan/result/write confirmation
- [ ] Network tools
- [ ] Settings/advanced access/about

## Accessibility foundation
- Minimum touch targets
- Semantic labels
- Dynamic font scaling
- Do not rely on color alone
- Logical focus order
- Screen-reader-friendly status text
- Sufficient contrast

## Gate
No major feature screen should be implemented before its state model and low-fidelity screen specification exist.
