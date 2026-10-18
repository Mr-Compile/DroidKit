package io.github.droidkit.feature.deviceinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
) : ViewModel()
