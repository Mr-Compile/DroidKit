package io.github.droidkit.feature.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    fun defaultSensor(type: Int): Sensor? = sensorManager?.getDefaultSensor(type)

    companion object {
        /** Sensor types safe to stream live without special permissions. */
        val LIVE_TYPES: Set<Int> = setOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_ROTATION_VECTOR,
        )

        fun unitFor(type: Int): String = when (type) {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION -> "m/s²"
            Sensor.TYPE_GYROSCOPE -> "rad/s"
            Sensor.TYPE_MAGNETIC_FIELD -> "µT"
            Sensor.TYPE_LIGHT -> "lx"
            Sensor.TYPE_PROXIMITY -> "cm"
            Sensor.TYPE_PRESSURE -> "hPa"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
            else -> ""
        }
    }
}
