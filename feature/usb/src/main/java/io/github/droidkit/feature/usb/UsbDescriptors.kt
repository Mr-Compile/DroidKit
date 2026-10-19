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
