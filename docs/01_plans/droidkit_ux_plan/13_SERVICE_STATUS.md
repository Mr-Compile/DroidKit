# Network, AI and Service Status

Add lightweight service status only if DroidKit has meaningful online dependencies.

Potential states:
- Available
- Offline
- Temporarily unavailable
- Not configured

Do not build a fake uptime dashboard.

Use real connectivity/service results where available and distinguish "device is offline" from "remote service failed" when possible.

A failed optional online service must not prevent unrelated offline DroidKit tools from functioning.
