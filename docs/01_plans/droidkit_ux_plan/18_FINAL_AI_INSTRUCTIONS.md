# Final Instructions for the Coding AI

You are modifying an existing DroidKit repository. Treat the repository as the source of truth.

## Execution sequence
1. Audit the repository.
2. Report relevant existing architecture/features.
3. Map this UX pack to existing code.
4. Identify conflicts, duplicates and non-applicable items.
5. Produce a concise implementation checklist.
6. Implement incrementally in the recommended order.
7. Reuse existing components and patterns.
8. Validate changed files/features after each logical phase.
9. Perform final integration QA.
10. Provide a truthful implementation report.

## Do not
- Rewrite the app from scratch.
- Replace architecture merely because you prefer another pattern.
- Invent functionality or content.
- Create fake buttons.
- Add unnecessary dependencies.
- Hardcode version/build information when metadata APIs exist.
- Hardcode secrets.
- Expose API keys.
- Claim an online action succeeded without performing it.
- Claim a feature was tested if it was not.
- Modify unrelated features unless necessary for safe integration.

## Final report format
### Repository Audit
Summarize relevant architecture discovered.

### Implemented
List completed UX improvements.

### Reused Existing Components
List shared components/services/design patterns reused.

### Files Changed
List created/modified files and their purpose.

### Navigation Changes
Describe routes and back behavior.

### Security & Privacy
Describe permission, data-sharing and secret-handling decisions.

### Validation Performed
List commands/tests/checks actually run and results.

### Requirement Matrix
PASS / PARTIAL / NOT IMPLEMENTED / NOT APPLICABLE for each major plan.

### Deferred Items
Explain anything intentionally not implemented and why.

### Issues Found
List pre-existing or newly discovered issues separately from work completed.

The goal is a cohesive DroidKit experience, not simply a larger Settings screen.
