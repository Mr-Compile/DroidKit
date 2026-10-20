# 18 — Motion & Interaction Feedback

Standardize press/ripple, progress, selection, expand/collapse and navigation feedback using existing Compose/Material behavior where possible.

Motion must:
- communicate hierarchy/state change
- remain brief
- respect reduced-motion/accessibility expectations where supported
- avoid decorative animation on technical workflows

Use snackbars for transient recoverable feedback, persistent banners/cards for persistent states, and dialogs for decisions.
