package io.github.droidkit.feature.diagnostics

import android.hardware.Sensor

data class PhoneDoctorTest(
    val id: String,
    val title: String,
    val category: String,
    val instructions: String,
    val sensorType: Int? = null,
)

object PhoneDoctorTests {

    val CORE: List<PhoneDoctorTest> = listOf(
        PhoneDoctorTest(
            id = "display.color",
            title = "Display colors",
            category = "Display",
            instructions = "Tap to cycle red, green, blue, white and black. " +
                "Check each color for uniform coverage, banding, tint shifts or dim patches.",
        ),
        PhoneDoctorTest(
            id = "display.dead_pixel",
            title = "Dead-pixel check",
            category = "Display",
            instructions = "Tap through every color and inspect the whole panel. " +
                "Look for pixels stuck on one color, always-dark dots, or bright specks.",
        ),
        PhoneDoctorTest(
            id = "touch.grid",
            title = "Touch grid",
            category = "Touch",
            instructions = "Drag your finger across every grid cell. " +
                "Cells fill where touch registers. Any cell that refuses to fill is a dead zone.",
        ),
        PhoneDoctorTest(
            id = "touch.multitouch",
            title = "Multitouch",
            category = "Touch",
            instructions = "Place several fingers on the screen at once. " +
                "Each finger should appear as its own marker. Count the simultaneous points.",
        ),
        PhoneDoctorTest(
            id = "haptics.vibration",
            title = "Vibration",
            category = "Haptics",
            instructions = "Tap Vibrate and confirm the device buzzes. " +
                "Repeat to check consistency. A weak or absent buzz usually means a motor or flex issue.",
        ),
        PhoneDoctorTest(
            id = "sensor.accelerometer",
            title = "Accelerometer",
            category = "Sensors",
            instructions = "Tilt the phone slowly in every direction. " +
                "The X/Y/Z values should change smoothly with orientation.",
            sensorType = Sensor.TYPE_ACCELEROMETER,
        ),
        PhoneDoctorTest(
            id = "sensor.gyroscope",
            title = "Gyroscope",
            category = "Sensors",
            instructions = "Rotate the phone around each axis. " +
                "Values should track rotation speed and settle near zero when still.",
            sensorType = Sensor.TYPE_GYROSCOPE,
        ),
        PhoneDoctorTest(
            id = "sensor.proximity",
            title = "Proximity sensor",
            category = "Sensors",
            instructions = "Cover and uncover the area near the earpiece. " +
                "The reading should switch between near and far values.",
            sensorType = Sensor.TYPE_PROXIMITY,
        ),
        PhoneDoctorTest(
            id = "sensor.light",
            title = "Light sensor",
            category = "Sensors",
            instructions = "Hold the phone under a lamp, then shade the sensor. " +
                "The lux value should rise and fall with the lighting.",
            sensorType = Sensor.TYPE_LIGHT,
        ),
        PhoneDoctorTest(
            id = "sensor.magnetometer",
            title = "Magnetometer",
            category = "Sensors",
            instructions = "Rotate the phone or move a magnet nearby. " +
                "Field strength values should respond to orientation and magnets.",
            sensorType = Sensor.TYPE_MAGNETIC_FIELD,
        ),
    )

    fun findById(id: String): PhoneDoctorTest? = CORE.firstOrNull { it.id == id }
}
