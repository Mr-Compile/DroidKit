# DroidKit Intelligence Architecture

## Architecture
Feature -> Structured Result -> AIContextBuilder -> PrivacyFilter -> AIUseCase -> AIProvider -> GroqProvider.

Features never depend directly on Groq.

## Planned assistants
- AI Technician
- Diagnostic Analyzer
- Repair Assistant
- Report Assistant
- Device Explainer
- USB Assistant
- NFC Assistant
- App Inspector Assistant
- Network Troubleshooter
- Tool Recommender
- Contextual Chat

## Action boundary
AI may explain, recommend a tool, summarize results, or propose structured parameters.

AI must not directly execute arbitrary shell commands, silently mutate settings, disable/uninstall packages, change privileged permissions, or perform destructive actions.

Mutations use:
AI proposal -> strict schema validation -> CapabilityManager -> risk classification -> explicit confirmation -> deterministic executor -> result.

## Failure independence
NO INTERNET -> core works.
NO KEY -> core works.
PROVIDER DOWN -> core works.
AI DISABLED -> core works.
