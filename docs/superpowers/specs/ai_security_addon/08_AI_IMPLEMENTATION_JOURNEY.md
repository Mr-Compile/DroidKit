# AI Implementation Journey

This is an add-on journey. Do not silently add it to a frozen V1 milestone.

## AI-0 Research
- [ ] Verify current Groq documentation.
- [ ] Choose BYOK/backend mode.
- [ ] Threat-model credentials/context.
- [ ] Define AIProvider.
- [ ] Define privacy UX.
- [ ] Define structured output schemas.

## AI-1 Core
- [ ] AIProvider
- [ ] GroqProvider
- [ ] AIContextBuilder
- [ ] PrivacyFilter
- [ ] PromptRegistry
- [ ] model/config registry
- [ ] typed errors
- [ ] timeout/retry policy
- [ ] secret redaction

## AI-2 Setup
- [ ] AI settings
- [ ] key entry if BYOK
- [ ] reviewed runtime storage
- [ ] test connection
- [ ] disconnect
- [ ] offline/no-key states

## AI-3 Assistants
- [ ] Device Explainer
- [ ] Diagnostic Analyzer
- [ ] Repair Assistant
- [ ] Report Assistant
- [ ] USB Assistant
- [ ] NFC Assistant
- [ ] App Assistant
- [ ] Network Troubleshooter
- [ ] Tool Recommender
- [ ] Contextual Chat

## AI-4 Structured Actions
- [ ] allow-listed action schema
- [ ] validator
- [ ] ToolRegistry mapping
- [ ] Risk Engine integration
- [ ] explicit confirmation
- [ ] reject arbitrary model commands

## AI-5 QA
- [ ] no/invalid key
- [ ] offline/timeout
- [ ] rate limit/provider outage
- [ ] malformed response
- [ ] prompt injection in tool data
- [ ] secret-redaction tests
- [ ] context-minimization review
- [ ] AI-disabled core regression
