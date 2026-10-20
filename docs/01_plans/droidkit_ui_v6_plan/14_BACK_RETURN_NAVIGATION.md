# 14 — Back & Return Navigation

Required behavior:
- Detail -> Back returns to origin with expected state.
- Settings child -> Back returns to Settings.
- Dialog/sheet -> Back dismisses overlay first.
- External Android settings -> return refreshes permission/provider/capability state without losing context.
- Deep link -> Back has a valid fallback.
- System back button/gesture matches visible navigation.
- Repeated taps do not stack duplicate copies of the same destination.
- Exiting the app happens only from an appropriate top-level state.

Test both toolbar Up and Android system back; they are related but not automatically identical.
