# Favorites and Recent Tools

## Favorites
Allow users to pin frequently used tools.

Requirements:
- Persist favorites using the project's existing preference/storage mechanism.
- Prevent duplicates.
- Remove favorites when a tool is permanently removed or gracefully ignore stale IDs.
- Provide accessible add/remove actions.
- Do not create a separate copy of tool definitions solely for favorites.

## Recent Tools
Track recently opened user-facing tools.

Requirements:
- Store stable tool identifiers, not fragile screen text.
- Avoid duplicate consecutive entries.
- Keep a reasonable bounded history.
- Do not treat Settings/About/Help navigation as recent tools unless the current product design intentionally does so.
- Provide a clear-history action only if useful.

## Home integration
If consistent with current home design, add compact Favorites and Recently Used sections without overwhelming the primary interface.
