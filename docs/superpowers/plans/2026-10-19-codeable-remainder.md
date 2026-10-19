# Codeable Remainder — J2 research, J18 leftovers, J21 AI batch

Date: 2026-10-19
Owner direction: implement every remaining item that does not require
physical hardware, testers, signing custody, or an owner product decision.

## Scope

In scope (code + docs + tests):
1. J2 leftover research items (checklist lines ~100–107) — captured as a
   research notes doc; several are already answered by the J18/J19 build.
2. J18 leftovers:
   - Advanced permission controls → `pm grant`/`pm revoke` as a typed
     `ElevatedAction.SetRuntimePermission`, surfaced in Permission Center.
   - Audit/result history → `ElevatedAuditSink` interface in
     `core:privileges`, Room-backed implementation in `core:database`
     (DB v1→v2 migration, `elevated_action_log` table), history card in
     Settings → Advanced access.
   - ADB terminal → NOT in scope (checklist: only if explicitly approved).
3. J21 remainder:
   - `PromptRegistry` — all system prompts centralized in `core:ai`.
   - Structured output schemas — `AiProposal` JSON contract + parser.
   - AI-3 assistants — shared `AiExplainButton` + `AiAssistViewModel` in
     `core:ai` (compose), wired into device info, diagnostics review,
     USB detail, NFC tag detail, app detail, network results. Contextual
     Chat screen reachable from Settings. Tool Recommender = chat
     proposals validated against `ToolRegistry`.
   - AI-4 structured actions — proposal types limited to `open_tool`
     (navigation) and read-only elevated actions (`GetProp`, `GetAppOps`,
     `ListPackages`, `WhoAmI`). No AI-triggered mutations. Confirmation
     dialog before execution regardless.
   - AI-5 — unit tests for gating, malformed output, injection-in-context,
     redaction, minimization, proposal validation.

Out of scope (unchanged): anything needing a device/emulator, J15/J16
release ops, J20 V2 work (needs owner PRD), the ADB terminal.

## Design notes

- `ElevatedOps` logs through `ElevatedAuditSink` (optional `Set` binding —
  empty set → no persistence). Keeps `core:privileges` free of Room.
- AI proposals are data, never commands: parsed into sealed
  `AiProposal`, validated against `ToolRegistry` + `CapabilityManager`,
  shown verbatim to the user, executed only on explicit tap.
- Assistant fields come from already-rendered screen data — same values
  the user sees, redacted by `PrivacyFilter` before leaving the device.
