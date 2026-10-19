# Global Search

## Goal
Allow users to quickly find tools, settings and help content as DroidKit grows.

## Requirements
Search should use actual app metadata rather than a disconnected duplicate list where practical.

Potential result groups:
- Tools
- Settings
- Help

Each result should have a clear title, optional short description/category and correct destination.

Support:
- Case-insensitive matching
- Useful keyword/alias matching when maintainable
- Empty query state
- No-results state
- Keyboard/search action
- Back navigation
- Accessibility labels

Do not expose hidden, unfinished, developer-only or inaccessible routes.

If search architecture would require major refactoring, implement the smallest maintainable index first and document future improvements.
