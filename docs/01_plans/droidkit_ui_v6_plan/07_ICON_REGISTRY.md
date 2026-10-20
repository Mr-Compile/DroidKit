# 07 — Icon Registry & Tool Metadata

Use one authoritative metadata path rather than mapping icons separately on each screen.

Conceptual model:
Tool ID -> name -> description -> category -> icon key -> search aliases -> route -> capability requirements.

Integrate with the existing ToolRegistry/feature contribution architecture instead of replacing it.

The same icon identity should appear in Home, Tools, Search, Favorites, Recents and tool-info screens.

Add validation/tests for missing icon mappings and duplicate/invalid tool identifiers where practical.
