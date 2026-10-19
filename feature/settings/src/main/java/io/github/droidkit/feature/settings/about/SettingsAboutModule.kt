package io.github.droidkit.feature.settings.about

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsAboutModule {
    @Binds
    abstract fun bindUpdateChecker(impl: GitHubReleasesUpdateChecker): UpdateChecker
}
