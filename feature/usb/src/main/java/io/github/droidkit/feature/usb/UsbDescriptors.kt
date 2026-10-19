package io.github.droidkit.feature.usb

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice

/** Pure formatting helpers for USB descriptors — framework-constant only, unit-testable. */
object UsbDescriptors {

    fun hex4(value: Int): String = "%04X".format(value and 0xFFFF)

    fun hex2(value: Int): String = "%02X".format(value and 0xFF)

    /** USB-IF base class names (bDeviceClass). */
    fun className(code: Int): String = when (code) {
        0x00 -> "Defined at interface level"
        0x01 -> "Audio"
        0x02 -> "Communications (CDC)"
        0x03 -> "HID (Human Interface Device)"
        0x05 -> "Physical"
        0x06 -> "Image (PTP)"
        0x07 -> "Printer"
        0x08 -> "Mass Storage"
        0x09 -> "Hub"
        0x0A -> "CDC Data"
        0x0B -> "Smart Card"
        0x0D -> "Content Security"
        0x0E -> "Video"
        0x0F -> "Personal Healthcare"
        0x10 -> "Audio/Video"
        0x11 -> "Billboard"
        0xDC -> "Diagnostic"
        0xE0 -> "Wireless Controller"
        0xEF -> "Miscellaneous"
        0xFE -> "Application Specific"
        0xFF -> "Vendor Specific"
        else -> "Unknown"
    }

    /** bEndpointAddress transfer type (bmAttributes & 0x03). */
    fun endpointTypeName(attributes: Int): String = when (attributes and 0x03) {
        0 -> "Control"
        1 -> "Isochronous"
        2 -> "Bulk"
        3 -> "Interrupt"
        else -> "Unknown"
    }

    fun directionName(direction: Int): String = when (direction) {
        UsbConstants.USB_DIR_IN -> "IN (device → host)"
        UsbConstants.USB_DIR_OUT -> "OUT (host → device)"
        else -> "Unknown"
    }

    /**
     * Interface-level interpretation — what a class/subclass/protocol triple
     * means in practice. Only statements justified by the USB-IF class codes
     * are returned; never claim internal detail Android cannot see.
     */
    fun interfaceInterpretation(cls: Int, subclass: Int, protocol: Int): String = when (cls) {
        0x01 -> "Audio" + when (subclass) {
            0x01 -> " control interface"
            0x02 -> " streaming (speaker/microphone data)"
            0x03 -> " MIDI streaming"
            else -> ""
        }
        0x02 -> "Communications" + when (subclass) {
            0x02 -> " — Abstract Control Model (modem/serial-style control channel)"
            0x04 -> " — Ethernet emulation (USB network adapter)"
            0x06 -> " — Ethernet Networking Control Model"
            0x0D -> " — Network Control Model (tethering)"
            else -> " control interface"
        }
        0x0A -> "CDC Data — bulk data channel, typically paired with a CDC control " +
            "interface (serial-style device such as a modem or adapter)"
        0x03 -> "HID" + when {
            subclass == 0x01 && protocol == 0x01 -> " boot keyboard"
            subclass == 0x01 && protocol == 0x02 -> " boot mouse"
            subclass == 0x01 -> " boot-interface device"
            else -> " (keyboard/mouse/controller-class input device)"
        }
        0x05 -> "Physical interface device (e.g. force-feedback hardware)"
        0x06 -> "Still image capture" + when (protocol) {
            0x01 -> " — PTP camera protocol"
            else -> ""
        }
        0x07 -> "Printer"
        0x08 -> "Mass storage" + when (protocol) {
            0x50 -> " — Bulk-Only Transport (typical flash drive/disk)"
            0x62 -> " — UAS (USB Attached SCSI — faster storage protocol)"
            else -> ""
        }
        0x09 -> "USB hub"
        0x0B -> "Smart card reader (CCID)"
        0x0D -> "Content security interface"
        0x0E -> "Video (UVC)" + when (subclass) {
            0x01 -> " control — webcam control interface"
            0x02 -> " streaming — webcam video stream"
            else -> ""
        }
        0x0F -> "Personal healthcare device"
        0x10 -> "Audio/video device"
        0x11 -> "Billboard — device reports an alternate-mode status"
        0xDC -> "Diagnostic device"
        0xE0 -> "Wireless controller" + when {
            subclass == 0x01 && protocol == 0x01 -> " — Bluetooth radio"
            subclass == 0x01 && protocol == 0x03 -> " — Bluetooth AMP"
            subclass == 0x02 -> " — UWB radio"
            else -> ""
        }
        0xEF -> "Miscellaneous" + when {
            subclass == 0x04 && protocol == 0x01 -> " — RNDIS (Windows-style network tethering)"
            else -> " (composite device grouping)"
        }
        0xFE -> "Application-specific" + when (subclass) {
            0x01 -> " — device firmware upgrade (DFU)"
            0x02 -> " — IRDA bridge"
            0x03 -> " — test & measurement (USBTMC)"
            else -> ""
        }
        0xFF -> "Vendor-specific protocol — interpretation requires vendor documentation"
        else -> "Unrecognized class"
    }

    /** Whole-device capability summary from its interface mix. */
    fun deviceInterpretation(interfaceTriples: List<Triple<Int, Int, Int>>): String {
        if (interfaceTriples.isEmpty()) return "No interfaces exposed."
        val labels = interfaceTriples.map { (c, s, p) -> interfaceInterpretation(c, s, p) }
        val classes = interfaceTriples.map { it.first }.toSet()
        return buildString {
            appendLine(labels.joinToString("\n") { "• $it" })
            if (0x03 in classes) appendLine("This device can act as an input peripheral.")
            if (0x08 in classes) appendLine("Android may mount storage volumes it exposes.")
            if (0x02 in classes || 0x0A in classes || (0xEF in classes)) {
                appendLine("Serial/modem-style interfaces usually need a vendor driver " +
                    "or app support — Android's generic USB API does not provide a serial console.")
            }
            if (0x0E in classes) appendLine("UVC video streams are not exposed through UsbManager; " +
                "apps that read webcams use the Camera2 external-camera path.")
        }.trim()
    }
}

/** Clipboard/share summary for a device; `serial` is read by the caller only when permitted. */
fun UsbDevice.summaryText(serial: String?): String = buildString {
    appendLine("USB device: ${productName ?: deviceName}")
    appendLine("Device path: $deviceName")
    appendLine("VID:PID: ${UsbDescriptors.hex4(vendorId)}:${UsbDescriptors.hex4(productId)}")
    appendLine("Class: ${UsbDescriptors.className(deviceClass)} (${UsbDescriptors.hex2(deviceClass)})")
    appendLine("Subclass/Protocol: ${UsbDescriptors.hex2(deviceSubclass)}/${UsbDescriptors.hex2(deviceProtocol)}")
    appendLine("USB version: ${version ?: "Not exposed"}")
    appendLine("Manufacturer: ${manufacturerName ?: "Not exposed"}")
    appendLine("Product: ${productName ?: "Not exposed"}")
    appendLine("Serial: ${serial ?: "Not exposed"}")
    appendLine("Interfaces: $interfaceCount")
}
