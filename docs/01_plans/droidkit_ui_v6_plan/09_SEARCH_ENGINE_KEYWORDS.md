# 09 — Search Engine & Keywords

Extend existing search rather than creating a competing engine.

Search fields may include:
- tool name
- category
- aliases/synonyms
- feature keywords
- settings names
- help titles
- diagnostic names

Examples:
Battery: battery, health, charging, charge, temperature, voltage, power, capacity.
NFC Lab: nfc, ndef, tag, contactless, scan, reader, writer.
USB Lab: usb, otg, device, host, accessory, connected device.

Requirements:
- case-insensitive
- sensible partial matching
- deterministic ranking
- exact/title matches rank above loose aliases
- no network requirement for local tool/settings search
- keywords live with authoritative metadata where possible
