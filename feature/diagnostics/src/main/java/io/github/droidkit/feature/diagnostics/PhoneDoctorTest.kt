package io.github.droidkit.feature.diagnostics

import android.Manifest
import android.hardware.Sensor
import androidx.camera.core.CameraSelector

data class PhoneDoctorTest(
    val id: String,
    val title: String,
    val category: String,
    val instructions: String,
    val sensorType: Int? = null,
    val permission: String? = null,
    val lensFacing: Int? = null,
    val capabilityKey: String? = null,
)

object PhoneDoctorTests {

    /** J08 core set — display, touch, haptics, sensors. */
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

    /** J09 hardware set — audio, camera, connectivity, power. */
    val HARDWARE: List<PhoneDoctorTest> = listOf(
        PhoneDoctorTest(
            id = "audio.speaker",
            title = "Speaker",
            category = "Audio",
            instructions = "Tap Play and confirm a clear tone comes from the speaker. " +
                "Repeat to check consistency. Distortion, crackle or silence are failures.",
        ),
        PhoneDoctorTest(
            id = "audio.microphone",
            title = "Microphone",
            category = "Audio",
            instructions = "Record a few seconds of speech, then play it back. " +
                "Confirm your voice is audible and clear. This test needs microphone permission.",
            permission = Manifest.permission.RECORD_AUDIO,
        ),
        PhoneDoctorTest(
            id = "camera.front",
            title = "Front camera",
            category = "Camera",
            instructions = "Check the front camera preview renders a live image. " +
                "Cover the lens briefly to confirm the feed is real.",
            permission = Manifest.permission.CAMERA,
            lensFacing = CameraSelector.LENS_FACING_FRONT,
        ),
        PhoneDoctorTest(
            id = "camera.rear",
            title = "Rear camera",
            category = "Camera",
            instructions = "Check the rear camera preview renders a live image. " +
                "Cover the lens briefly to confirm the feed is real.",
            permission = Manifest.permission.CAMERA,
            lensFacing = CameraSelector.LENS_FACING_BACK,
        ),
        PhoneDoctorTest(
            id = "camera.flash",
            title = "Flash",
            category = "Camera",
            instructions = "Toggle the torch on and off while watching the rear of the device. " +
                "The LED should light clearly.",
            permission = Manifest.permission.CAMERA,
        ),
        PhoneDoctorTest(
            id = "connectivity.wifi",
            title = "Wi-Fi",
            category = "Connectivity",
            instructions = "Checks Wi-Fi hardware capability and whether the radio is enabled. " +
                "The capability result records automatically.",
            capabilityKey = "wifi",
        ),
        PhoneDoctorTest(
            id = "connectivity.bluetooth",
            title = "Bluetooth",
            category = "Connectivity",
            instructions = "Checks Bluetooth hardware capability and adapter state. " +
                "The capability result records automatically.",
            capabilityKey = "bluetooth",
        ),
        PhoneDoctorTest(
            id = "connectivity.location",
            title = "Location / GPS",
            category = "Connectivity",
            instructions = "Checks GNSS capability, provider state and the cached fix. " +
                "Tap Get live fix to prove GNSS acquires a position. Needs location permission; " +
                "the capability result records automatically.",
            capabilityKey = "location",
        ),
        PhoneDoctorTest(
            id = "connectivity.nfc",
            title = "NFC",
            category = "Connectivity",
            instructions = "Checks NFC hardware capability and adapter state. " +
                "The capability result records automatically.",
            capabilityKey = "nfc",
        ),
        PhoneDoctorTest(
            id = "connectivity.usb",
            title = "USB",
            category = "Connectivity",
            instructions = "Checks USB host/accessory capability. " +
                "The capability result records automatically.",
            capabilityKey = "usb",
        ),
        PhoneDoctorTest(
            id = "power.charging",
            title = "Charging / battery",
            category = "Power",
            instructions = "Review the battery readings. Plug in a charger and confirm the " +
                "charging state changes to charging and the plug source appears.",
        ),
    )

    val ALL: List<PhoneDoctorTest> = CORE + HARDWARE

    /**
     * Curated subset for used-phone intake: the buyer-visible essentials —
     * screen, touch, audio, cameras, flash, radios and charging — minus the
     * deeper sensor sweep.
     */
    val USED_PHONE: List<PhoneDoctorTest> = ALL.filter {
        it.id in setOf(
            "display.color", "display.dead_pixel",
            "touch.grid", "touch.multitouch",
            "haptics.vibration",
            "audio.speaker", "audio.microphone",
            "camera.front", "camera.rear", "camera.flash",
            "connectivity.wifi", "connectivity.bluetooth",
            "connectivity.nfc", "connectivity.usb",
            "power.charging",
        )
    }

    fun findById(id: String): PhoneDoctorTest? = ALL.firstOrNull { it.id == id }
}

enum class DiagnosticPreset(val tests: List<PhoneDoctorTest>, val label: String) {
    FULL(PhoneDoctorTests.ALL, "Full diagnostic"),
    USED_PHONE(PhoneDoctorTests.USED_PHONE, "Used-phone check"),
}
