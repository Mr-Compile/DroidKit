package io.github.droidkit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.permissions.PermissionChecker
import io.github.droidkit.core.permissions.PermissionManager
import io.github.droidkit.core.privileges.AdbSessionSetup
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ElevatedExecutor
import io.github.droidkit.core.privileges.ProviderSetup
import io.github.droidkit.core.system.DeviceSystemProbe
import io.github.droidkit.feature.adb.AdbFeature
import io.github.droidkit.feature.appops.AppOpsFeature
import io.github.droidkit.feature.apps.AppsFeature
import io.github.droidkit.feature.audio.AudioFeature
import io.github.droidkit.feature.battery.BatteryFeature
import io.github.droidkit.feature.bluetooth.BluetoothFeature
import io.github.droidkit.feature.camera.CameraFeature
import io.github.droidkit.feature.dashboard.DashboardFeature
import io.github.droidkit.feature.developer.DeveloperFeature
import io.github.droidkit.feature.deviceinfo.DeviceInfoFeature
import io.github.droidkit.feature.diagnostics.DiagnosticsFeature
import io.github.droidkit.feature.display.DisplayFeature
import io.github.droidkit.feature.insights.InsightsFeature
import io.github.droidkit.feature.network.NetworkFeature
import io.github.droidkit.feature.nfc.NfcFeature
import io.github.droidkit.feature.packages.PackagesFeature
import io.github.droidkit.feature.permissions.PermissionsFeature
import io.github.droidkit.feature.reports.ReportsFeature
import io.github.droidkit.feature.sensors.SensorsFeature
import io.github.droidkit.feature.settings.SettingsFeature
import io.github.droidkit.feature.tools.ToolsFeature
import io.github.droidkit.feature.usb.UsbFeature
import io.github.droidkit.privilege.adb.WirelessAdbProvider
import io.github.droidkit.privilege.root.RootProvider
import io.github.droidkit.privilege.standard.StandardAndroidProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProbe(@ApplicationContext context: Context) = DeviceSystemProbe(context)

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context) = PermissionManager(context)

    @Provides
    fun providePermissionChecker(manager: PermissionManager): PermissionChecker = manager

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) = SettingsRepository(context)

    @Provides
    @Singleton
    fun provideAdbProvider(@ApplicationContext context: Context) = WirelessAdbProvider(context)

    @Provides
    @Singleton
    fun provideAdbSetup(adb: WirelessAdbProvider): AdbSessionSetup = adb

    @Provides
    @Singleton
    fun provideRootProvider() = RootProvider()

    @Provides
    @Singleton
    fun provideProviders(
        adb: WirelessAdbProvider,
        root: RootProvider,
    ): Set<CapabilityProvider> = setOf(
        StandardAndroidProvider(),
        adb,
        root,
    )

    @Provides
    @Singleton
    fun provideExecutors(
        adb: WirelessAdbProvider,
        root: RootProvider,
    ): Set<ElevatedExecutor> = setOf(root, adb)

    @Provides
    @Singleton
    fun provideSetups(
        root: RootProvider,
    ): Set<ProviderSetup> = setOf(root)

    @Provides
    @Singleton
    fun provideCapabilityManager(
        providers: Set<@JvmSuppressWildcards CapabilityProvider>,
        probe: DeviceSystemProbe,
        permissionChecker: PermissionChecker,
    ) = CapabilityManager(
        providers = providers,
        sdkInt = probe.sdkInt,
        hasFeature = probe::hasFeature,
        permissionChecker = permissionChecker,
    )

    @Provides
    @Singleton
    fun provideContributions(): Set<FeatureContribution> = setOf(
        DashboardFeature,
        ToolsFeature,
        DiagnosticsFeature,
        ReportsFeature,
        InsightsFeature,
        SettingsFeature,
        DeviceInfoFeature,
        BatteryFeature,
        DisplayFeature,
        AudioFeature,
        CameraFeature,
        SensorsFeature,
        NetworkFeature,
        BluetoothFeature,
        UsbFeature,
        NfcFeature,
        AppsFeature,
        PermissionsFeature,
        AppOpsFeature,
        PackagesFeature,
        AdbFeature,
        DeveloperFeature,
    )

    @Provides
    @Singleton
    fun provideToolRegistry(contributions: Set<@JvmSuppressWildcards FeatureContribution>) = ToolRegistry(contributions)
}
