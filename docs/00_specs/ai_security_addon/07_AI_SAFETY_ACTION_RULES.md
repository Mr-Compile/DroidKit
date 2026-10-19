# AI Safety and Action Rules

AI output is not proof of hardware failure, malware, security compromise, or repair necessity.

Raw deterministic DroidKit results remain the source of truth. AI should distinguish observation, possible explanation, recommendation, and uncertainty.

## Execution
User request -> AI recommendation/proposal -> deterministic validation -> risk screen -> user approval -> CapabilityManager -> approved provider -> result.

## Never
- Execute arbitrary model-generated shell directly.
- Let AI bypass confirmations.
- Accept invented targets without validation.
- Let AI invoke hidden/root behavior outside approved capabilities.
- Let AI claim unsupported values were measured.
- Send secrets as model context.
