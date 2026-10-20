# 25 — UI & Navigation QA

## Visual
- [ ] consistent headers/cards/icons/chips
- [ ] no clipping/overflow
- [ ] light/dark valid
- [ ] large text valid

## Icons/Search
- [ ] every visible tool has correct semantic icon
- [ ] no mixed/random icon families
- [ ] same tool uses same icon across surfaces
- [ ] aliases find expected tools
- [ ] local search works offline
- [ ] empty/no-result behavior is clear

## Status
- [ ] status text matches verified domain state
- [ ] no color-only state
- [ ] permission/provider/capability concepts not conflated
- [ ] status refreshes after returning from settings/grant flows

## Navigation
- [ ] Home/Tools/Diagnose/Settings behavior correct
- [ ] toolbar Up correct
- [ ] system back/gesture correct
- [ ] dialogs/sheets dismiss correctly
- [ ] search/list state restored
- [ ] no duplicate destinations/loops
- [ ] invalid route arguments fail safely

## Accessibility
- [ ] touch targets adequate
- [ ] meaningful semantics
- [ ] decorative icons excluded
- [ ] large fonts and contrast checked

## Validation rule
Run targeted checks first. Broaden validation when shared UI/navigation contracts affect multiple modules.
