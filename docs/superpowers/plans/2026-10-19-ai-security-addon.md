# AI + Secrets Security Add-on — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement Part A task-by-task. Part B is a post-V1 journey: write its detailed plan at AI-0, do not execute now.

**Goal:** Adopt `specs/ai_security_addon/` — Part A lands the secrets/GitHub hardening that applies to all code starting now (before the J05 skeleton); Part B registers the AI/Groq journey for post-V1 without touching the frozen V1 scope.

**Spec:** `docs/superpowers/specs/ai_security_addon/` (extracted from `DroidKit_AI_Groq_Security_Addon_v3.zip`, zip deleted as redundant copy). The addon's own precedence: verified reality > AGENTS.md/security rules > **this addon for AI+secret topics** > v2 feature plans > original plans.

**Non-negotiable (spec):** DroidKit core tools work without AI, internet, or a Groq key. AI explains/recommends/summarizes; deterministic code performs diagnostics and actions. "Do not silently add it to a frozen V1 milestone."

---

## Part A — Secrets/GitHub hardening (run now, before J05)

Small doc/config chore on `main`, same workflow as Q2/AGENTS.md adoption.

### Task A1: `.gitignore` secrets entries

- Modify: root `.gitignore`
- [x] **Step 1:** Merge `specs/ai_security_addon/.gitignore.additions` — `.env`, `.env.*` (except `!.env.example`), `local.properties`, `*.jks`/`*.keystore`/`*.p12`/`*.pfx`/`*.pem`/`*.key`, `keystore.properties`, `signing.properties`, `.secrets/`, `secrets/`. Skip entries already covered (`local.properties`, `.gradle/`, `build/`).
- [x] **Step 2: Verify** — `git check-ignore .env keystore.properties release.jks` exits 0 for each.
- [x] **Step 3: Commit** — `chore: ignore secrets and signing material`

### Task A2: `.env.example` at root

- Create: root `.env.example` from `specs/ai_security_addon/.env.example` verbatim — placeholders only (`GROQ_API_KEY=YOUR_GROQ_API_KEY_HERE`, `DROIDKIT_AI_PROVIDER=groq`, `DROIDKIT_AI_BASE_URL=...invalid`).
- [x] **Step 1:** Copy + commit `chore: add .env.example with placeholders`.
- [x] **Step 2: Self-check** — file contains zero real values (spec rule: `.env.example` never contains a valid key).

### Task A3: Merge AI/secrets rules into `AGENTS.md`

- Modify: root `AGENTS.md`
- Source: `specs/ai_security_addon/09_AGENTS_AI_SECURITY_ADDON.md`
- [x] **Step 1:** Add a `## AI and secrets` section — provider abstraction (features depend on `AIProvider`, never Groq), AI optional, secrets never created/pasted/logged/committed, `.env` ignored, no production secrets in client source/BuildConfig, BYOK storage requires platform-security review, **audit the diff for secrets before every commit/push**, committed credential → rotate/revoke + history remediation.
- [x] **Step 2:** Source-of-truth list: insert `specs/ai_security_addon/` for AI/secret topics per the addon's precedence.
- [x] **Step 3:** Add `10_SECRET_INCIDENT_PLAYBOOK.md` to the read-before-editing/security references (canonical copy stays at `specs/ai_security_addon/`).
- [x] **Step 4: Commit** — `docs: merge AI + secrets rules into AGENTS.md`

### Task A4: Register in queue + tracker

- Modify: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md` — add **Q6b** (this Part A, chore) and **Q23** (Part B journey, Phase E).
- Modify: `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md` — append "Journey 21 — DroidKit Intelligence (AI add-on)" after J20, with the AI-0..AI-5 items from `08_AI_IMPLEMENTATION_JOURNEY.md`, header noting: post-V1, not part of frozen scope.
- [x] **Step 1:** Edits + commit `docs: register AI security addon (Q6b + J21/Q23)`.

### Part A gate

- [x] `git check-ignore` covers `.env`, keystores, signing files
- [x] `.env.example` tracked with placeholders only
- [x] `AGENTS.md` carries the AI/secrets rules and the diff-scan-before-commit rule
- [x] Q6b + Q23 registered; J21 in tracker marked post-V1

---

## Part B — J21 AI Intelligence journey (post-V1, NOT now)

Queue as **Q23**, after Q22/J20 decisions. The spec requires research before freezing any detail ("verify current official Groq API documentation… do not freeze old model names into planning") — so the code-level plan is written at AI-0, not now.

Roadmap (from `08_AI_IMPLEMENTATION_JOURNEY.md`):

| Phase | Content | Gate |
|---|---|---|
| AI-0 Research | Verify current Groq docs; BYOK vs hosted-backend mode; threat-model credentials/context; `AIProvider` contract; privacy UX; structured output schemas | Mode decision recorded; threat model written |
| AI-1 Core | `AIProvider`, `GroqProvider`, `AIContextBuilder`, `PrivacyFilter`, `PromptRegistry`, model/config registry, typed errors (`NO_KEY/OFFLINE/AUTH_ERROR/RATE_LIMITED/PROVIDER_ERROR/MALFORMED_RESPONSE/DISABLED`), timeout/retry, secret redaction | Core compiles with AI disabled; redaction tests pass |
| AI-2 Setup | Settings → DroidKit Intelligence: enable toggle, BYOK key entry, reviewed runtime storage, Test Connection, Clear/Disconnect, offline/no-key states | Never reveals full stored key |
| AI-3 Assistants | Device Explainer, Diagnostic Analyzer, Repair Assistant, Report Assistant, USB/NFC/App assistants, Network Troubleshooter, Tool Recommender, Contextual Chat | Each sends minimum necessary context only |
| AI-4 Structured Actions | Allow-listed action schema + validator, ToolRegistry mapping, Risk Engine integration, explicit confirmation; reject arbitrary model commands | No path executes model-generated shell |
| AI-5 QA | No/invalid key, offline/timeout, rate limit/outage, malformed response, **prompt injection via tool data** (NFC/USB/app metadata treated as untrusted data), secret-redaction tests, context-minimization review, AI-disabled core regression | `11_AI_SECURITY_ACCEPTANCE_CHECKLIST.md` all green |

Safety invariants to enforce in the detailed plan:

- Mutation flow only: `AI proposal → strict schema validation → CapabilityManager → risk classification → explicit confirmation → deterministic executor`.
- Never send: keys/tokens/private keys, contacts/messages, personal files, unrelated clipboard/logs, ADB private keys, credentials.
- BYOK: user-entered key uses reviewed Android credential storage — never `.env`, BuildConfig, resources, logs, reports, or plaintext.
- Hosted mode (future commercial): Groq key lives server-side only, behind authenticated backend with quotas/rate limits.

**Gate for Q23 start:** owner approves scope + mode (BYOK vs backend). Version: minor post-v1 (e.g. v1.6.0) — never a V1 scope change.
