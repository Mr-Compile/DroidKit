# 24 — Migration From UI v4/v5

Do not rewrite all screens at once.

1. Audit and freeze v6 contracts.
2. Implement shared components/metadata adapters.
3. Migrate one representative tool list + one detail screen + Settings/search.
4. Validate visual/navigation/status behavior.
5. Migrate remaining screens in batches.
6. Remove old duplicate components only after no callers remain.

Keep a migration table: Screen / Current pattern / v6 target / Status / Notes.
