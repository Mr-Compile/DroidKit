package io.github.droidkit.feature.diagnostics

/**
 * Routing classification for a Phone Doctor catalog entry. Pure function so the
 * "every catalog test is wired" invariant (DK-050) is unit-testable without Compose.
 */
enum class PhoneDoctorRoute {
    DisplayColor,
    TouchGrid,
    Multitouch,
    Vibration,
    Speaker,
    Microphone,
    CameraPreview,
    Flash,
    ConnectivityCheck,
    LocationCheck,
    ChargingCheck,
    HeadsetJack,
    VolumeButtons,
    Sensor,
    Unsupported,
}

fun routeFor(test: PhoneDoctorTest): PhoneDoctorRoute = when {
    test.id.startsWith("display.") -> PhoneDoctorRoute.DisplayColor
    test.id == "touch.grid" -> PhoneDoctorRoute.TouchGrid
    test.id == "touch.multitouch" -> PhoneDoctorRoute.Multitouch
    test.id == "haptics.vibration" -> PhoneDoctorRoute.Vibration
    test.id == "audio.speaker" -> PhoneDoctorRoute.Speaker
    test.id == "audio.microphone" -> PhoneDoctorRoute.Microphone
    test.id == "camera.flash" -> PhoneDoctorRoute.Flash
    test.id.startsWith("camera.") -> PhoneDoctorRoute.CameraPreview
    test.id == "connectivity.location" -> PhoneDoctorRoute.LocationCheck
    test.capabilityKey != null -> PhoneDoctorRoute.ConnectivityCheck
    test.id == "power.charging" -> PhoneDoctorRoute.ChargingCheck
    test.id == "audio.headset_jack" -> PhoneDoctorRoute.HeadsetJack
    test.id == "buttons.volume" -> PhoneDoctorRoute.VolumeButtons
    test.sensorType != null -> PhoneDoctorRoute.Sensor
    else -> PhoneDoctorRoute.Unsupported
}
