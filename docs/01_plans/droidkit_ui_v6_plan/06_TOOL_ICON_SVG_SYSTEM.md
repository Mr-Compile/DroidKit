# 06 — Tool Icon & SVG/Vector System

## Rules
1. Audit existing Material/vector icons first.
2. Prefer existing Android/Material vector icons for universal actions: back, search, close, settings, info, favorite, more, share.
3. Use custom DroidKit vector/SVG assets only when a specialized tool lacks a clear standard icon.
4. Do not download random icon packs or mix visual families.
5. Convert/import SVG safely into the Android vector/Compose asset approach already used by the project.
6. Keep consistent stroke/weight, optical size, padding and icon container treatment.
7. Never alter the official DroidKit logo.

## Required tool coverage
Audit all user-visible tools and assign a semantic icon, including device info, CPU, memory, storage, battery, display, sensors, camera, microphone, speaker, vibration, Wi-Fi, Bluetooth, network, location/GPS, USB Lab, NFC Lab, App Inspector, Permission Inspector, Diagnostics, Reports, AI and Settings where present.

## Specialized custom candidates
NFC Lab, USB Lab, Phone Doctor, App Inspector, Device Inspector and advanced provider tools may receive custom vectors if standard icons are ambiguous.

## Accessibility
Decorative icons should not produce duplicate announcements. Icon-only actions require meaningful content descriptions.
