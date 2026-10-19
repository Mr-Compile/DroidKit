# Friendly Error Catalog
Final strings belong in Android resources/localization.

## Permission
`Microphone access needed` — DroidKit needs access to run the microphone diagnostic. Actions: Allow Access / Not Now. Permanently denied -> Open App Settings.

## Unsupported
`NFC isn't available on this device.` Result is NOT_SUPPORTED, not FAIL.

## Disabled
`NFC is turned off.` Action: Open NFC Settings.

## Empty
`No USB device connected.` Explain OTG connection. Not an error.
`No reports yet.` Explain where saved diagnostics appear.

## USB
Permission: `USB permission needed.`
Disconnect: `USB inspection stopped. DroidKit lost access to the device.` Try Again.

## Bluetooth
Off -> Open Bluetooth Settings. No devices found -> Empty, not Error.

## Network
Distinguish no network, DNS failure, target unreachable, timeout and restricted information.

## Shizuku
Distinguish not installed, not running, permission not granted, disconnected and unsupported capability.

## Wireless ADB
Distinguish unsupported version, pairing required, pairing failed, connection lost and authorization failure.

## Groq/AI
NO_KEY -> Connect an AI provider.
AUTH_ERROR -> This API key wasn't accepted.
OFFLINE -> Intelligence needs internet; Continue Without AI.
RATE_LIMITED -> Provider is receiving too many requests; Try Later.
TIMEOUT -> Provider took too long; Try Again.
PROVIDER_ERROR -> Intelligence temporarily unavailable.
MALFORMED_RESPONSE -> DroidKit couldn't safely interpret the AI response; execute nothing.

## Critical mutation
Show target, exact consequence, reversibility and explicit confirmation. Never vague `Continue`.
