# Tool Availability & Capability Detection

## Goal
Explain unsupported tools before users enter a broken flow.

Where appropriate, evaluate:
- Hardware capability
- Android/API level
- Required system feature
- Permission/access state
- Root requirement if an existing tool genuinely requires root
- Connectivity requirement
- Service availability

Keep unsupported tools visible when discovery is valuable, but clearly explain why they cannot currently run.

Examples of user-facing states:
- Not supported on this device
- Requires Android [version] or later
- Permission required
- Internet connection required
- Root access required

Capability checks must reflect real platform APIs, not device-name guesses.
