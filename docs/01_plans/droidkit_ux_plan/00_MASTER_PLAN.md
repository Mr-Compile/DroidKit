# DroidKit UX Modernization — Master Plan

## Goal
Turn DroidKit from a collection of utilities into a cohesive, understandable, trustworthy Android utility product.

## Mandatory pre-implementation audit
Before changing code:
1. Identify project language/framework and module structure.
2. Identify all existing tools/features and their routes.
3. Identify Settings implementation and navigation architecture.
4. Identify design system, reusable components, spacing, typography, icons, theme tokens, dialogs, snackbars, loading states and empty states.
5. Identify state-management and persistence mechanisms.
6. Identify permission handling and special Android access flows.
7. Identify networking, API, AI/Groq, update, analytics and crash-reporting integrations.
8. Identify existing help/about/privacy/changelog/search/favorite/recent implementations.
9. Identify tests relevant to files that will change.
10. Produce a short audit report before implementation.

## Rules
- Reuse before creating.
- Never introduce a second navigation/state/design system.
- Do not invent URLs, legal documents, support addresses, developer names, release history, features or API endpoints.
- Do not advertise unfinished tools.
- Do not request permissions until required by a user action unless Android/platform behavior genuinely requires otherwise.
- Local features must remain usable offline where possible.
- Never expose API keys or secrets in the client, source repository, logs or UI.
- Preserve existing functionality and navigation behavior.
- Use friendly user-facing errors while retaining technical diagnostics only in appropriate internal logs.
- Every new UI must support the app's existing light/dark themes.
- Accessibility is a requirement, not a later polish pass.

## Implementation phases
### Phase 1 — Foundation
Standardize reusable UX states, external-link handling, capability status, metadata access and common settings rows.

### Phase 2 — Discovery
Implement global search, favorites, recents and scalable tool categorization.

### Phase 3 — Trust
Implement permissions, privacy, tool explanations and transparent AI/network behavior.

### Phase 4 — Assistance
Implement Help/FAQ, problem reporting, feedback and useful troubleshooting entry points.

### Phase 5 — Product identity
Implement About DroidKit, changelog, licenses and developer/product information.

### Phase 6 — Accessibility and resilience
Audit semantics, touch targets, font scaling, unsupported states, offline behavior and settings recovery.

### Phase 7 — QA
Perform targeted tests based on changed files/features, integration checks, build validation and a final requirements audit.

## Definition of success
Users should be able to answer these questions from the UI:
- What can DroidKit do?
- How do I find a tool?
- Why does this tool need permission?
- Will this send information online?
- Is my device capable of using this feature?
- What happened when something failed?
- How do I get help?
- What version am I using?
