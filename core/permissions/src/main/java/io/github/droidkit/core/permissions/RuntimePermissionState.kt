package io.github.droidkit.core.permissions

/**
 * Lifecycle states for an Android runtime permission (DK-021).
 *
 * Android exposes only "denied + shouldShowRationale"; "permanently denied"
 * can only be concluded after a request round-trip returns denied with no
 * rationale. The Compose gate in core:ui tracks that transition.
 */
enum class RuntimePermissionState {
    /** Held — proceed. */
    GRANTED,

    /** Not held (or not yet asked this session) — a system request may still show. */
    DENIED_REQUESTABLE,

    /** Denied with rationale gone after a request — only system Settings can fix it. */
    DENIED_PERMANENT,

    /** Not in the merged manifest — asking would be a no-op; treat as honest failure. */
    NOT_DECLARED,
}
