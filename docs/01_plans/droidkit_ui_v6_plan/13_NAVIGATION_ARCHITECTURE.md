# 13 — Navigation Architecture

Preserve the existing navigation framework and top-level structure unless repository reality proves it changed.

Expected top-level concept: Home | Tools | Diagnose | Settings.

Audit every route for:
- stable route naming
- argument encoding/decoding
- invalid/missing arguments
- duplicate destinations
- direct route construction scattered through UI
- deep-link behavior
- navigation loops
- duplicate rapid taps

Pay special attention to any previously identified dynamic sensor/live route and verify the actual current implementation before fixing it.
