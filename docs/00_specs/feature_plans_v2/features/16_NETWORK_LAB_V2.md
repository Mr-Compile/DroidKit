# Network Lab v2

## V1 CORE
Active transport summary, permitted local/network IP information, DNS servers where available, ping/reachability, DNS lookup, basic connection diagnostics, copy/export.

## Tools
Connection Inspector; Ping; DNS Lookup; Host Reachability; Network Snapshot.

## Improved diagnostic
Network available? Internet capability? DNS resolution? Target reachable? Latency samples? Report each layer separately instead of a single fake 'network health' score.

## POST-V1
Port reachability for user-specified legitimate endpoints, route/interface detail where Android permits, history/comparison, local service discovery where safe and justified.

## Boundaries
Android increasingly restricts Wi-Fi identifiers and network data. Do not imply access to restricted MAC/BSSID/SSID fields without correct permissions/API conditions.

## Shared implementation requirements
- Explicit loading/content/permission-required/unsupported/error states.
- Hardware absence must not be reported as a failed test.
- Never fabricate inaccessible Android values.
- Every mutating operation requires a preview/confirmation appropriate to risk.
- Register every sub-tool in ToolRegistry where it is independently launchable.
- CapabilityManager owns elevated-provider selection.
- Keep business logic out of Compose UI.
- Record known OEM/API limitations.

## Completion gate
- [ ] Feasibility verified.
- [ ] Scope tier confirmed.
- [ ] UX states implemented.
- [ ] Permissions handled.
- [ ] Unsupported hardware/API handled.
- [ ] Targeted validation passed.
- [ ] Real-device validation completed where hardware-dependent.
- [ ] Documentation/checklist updated.
