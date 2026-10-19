package io.github.droidkit.feature.diagnostics

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.droidkit.core.common.TestTitleLookup

@Module
@InstallIn(SingletonComponent::class)
object DiagnosticsModule {

    /** Exposes the Phone Doctor catalog titles to other features (Insights). */
    @Provides
    fun provideTestTitleLookup(): TestTitleLookup =
        TestTitleLookup { id -> PhoneDoctorTests.findById(id)?.title }
}
