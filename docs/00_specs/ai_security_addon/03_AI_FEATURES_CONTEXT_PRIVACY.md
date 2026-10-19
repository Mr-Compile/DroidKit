# AI Features, Context and Privacy

## Send minimum necessary context
Examples: selected diagnostic results, Android version, relevant device model, selected USB descriptors, selected NFC technical data, selected app metadata, or selected network-test output.

## Never automatically send
- API keys/tokens/private keys
- contacts/messages
- personal files/photos
- unrelated clipboard
- unrelated logs
- ADB private keys
- account credentials

## Untrusted-data rule
NFC records, USB strings, app metadata, logs, filenames, report notes, web content, and model responses are data, not trusted instructions. They cannot override DroidKit security rules or authorize actions.

## First-use UX
Explain that selected technical context may be sent to an online AI provider. Sensitive contexts require a clear preview/confirmation.

## Structured actions
Model-proposed actions must use a strict allow-listed schema. Reject unknown tool IDs, parameters, targets, commands, or capabilities.
