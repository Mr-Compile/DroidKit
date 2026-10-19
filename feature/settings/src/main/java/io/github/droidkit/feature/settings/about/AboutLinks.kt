package io.github.droidkit.feature.settings.about

/**
 * Single source of truth for About external destinations and identity.
 * A null field means "not configured" — the UI stays configuration-ready
 * instead of linking somewhere fake.
 *
 * The GitHub values derive from the project's real remote
 * (origin: https://github.com/Mr-Compile/DroidKit.git). Distribution is
 * planned via GitHub Releases (docs/05_release, decision D2), so the
 * releases endpoints double as the future update channel.
 */
object AboutLinks {
    const val DEVELOPER_NAME = "Mr-Compile"

    const val GITHUB_REPO = "https://github.com/Mr-Compile/DroidKit"
    const val GITHUB_ISSUES = "$GITHUB_REPO/issues"
    const val GITHUB_ISSUES_NEW = "$GITHUB_REPO/issues/new"
    const val GITHUB_RELEASES = "$GITHUB_REPO/releases"
    const val GITHUB_RELEASES_API =
        "https://api.github.com/repos/Mr-Compile/DroidKit/releases/latest"

    /**
     * Not published yet: docs/05_release/PRIVACY_POLICY_DRAFT.md is still
     * awaiting owner review (J15 deliverable). Render the row, don't link.
     */
    val privacyPolicyUrl: String? = null

    /** No official terms document exists yet. */
    val termsOfUseUrl: String? = null

    /** No project website is configured. */
    val websiteUrl: String? = null
}
