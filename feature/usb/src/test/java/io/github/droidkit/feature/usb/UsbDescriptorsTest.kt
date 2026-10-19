package io.github.droidkit.feature.usb

import org.junit.Assert.assertEquals
import org.junit.Test

class UsbDescriptorsTest {

    @Test
    fun `hex4 pads vendor and product ids`() {
        assertEquals("0BDA", UsbDescriptors.hex4(0x0BDA))
        assertEquals("0049", UsbDescriptors.hex4(0x49))
        assertEquals("FFFF", UsbDescriptors.hex4(0x1FFFF))
    }

    @Test
    fun `hex2 formats single byte`() {
        assertEquals("08", UsbDescriptors.hex2(0x08))
        assertEquals("FF", UsbDescriptors.hex2(0xFF))
    }

    @Test
    fun `className covers common USB-IF classes`() {
        assertEquals("Mass Storage", UsbDescriptors.className(0x08))
        assertEquals("HID (Human Interface Device)", UsbDescriptors.className(0x03))
        assertEquals("Hub", UsbDescriptors.className(0x09))
        assertEquals("Vendor Specific", UsbDescriptors.className(0xFF))
        assertEquals("Unknown", UsbDescriptors.className(0x77))
    }

    @Test
    fun `endpointTypeName reads transfer type bits`() {
        assertEquals("Control", UsbDescriptors.endpointTypeName(0))
        assertEquals("Isochronous", UsbDescriptors.endpointTypeName(1))
        assertEquals("Bulk", UsbDescriptors.endpointTypeName(2))
        assertEquals("Interrupt", UsbDescriptors.endpointTypeName(3))
        // attributes carry extra bits in the upper nibble — mask keeps type
        assertEquals("Bulk", UsbDescriptors.endpointTypeName(0x12))
    }

    @Test
    fun `directionName maps usb direction constants`() {
        assertEquals("IN (device → host)", UsbDescriptors.directionName(0x80))
        assertEquals("OUT (host → device)", UsbDescriptors.directionName(0x00))
        assertEquals("Unknown", UsbDescriptors.directionName(0x40))
    }

    @Test
    fun `interfaceInterpretation identifies common device profiles`() {
        assertEquals(
            "HID boot keyboard",
            UsbDescriptors.interfaceInterpretation(0x03, 0x01, 0x01),
        )
        assertEquals(
            "Mass storage — Bulk-Only Transport (typical flash drive/disk)",
            UsbDescriptors.interfaceInterpretation(0x08, 0x06, 0x50),
        )
        assertEquals(
            "Wireless controller — Bluetooth radio",
            UsbDescriptors.interfaceInterpretation(0xE0, 0x01, 0x01),
        )
        assertEquals(
            "Video (UVC) streaming — webcam video stream",
            UsbDescriptors.interfaceInterpretation(0x0E, 0x02, 0x00),
        )
    }

    @Test
    fun `interfaceInterpretation never fabricates unknown classes`() {
        assertEquals(
            "Unrecognized class",
            UsbDescriptors.interfaceInterpretation(0x77, 0x00, 0x00),
        )
        assert(
            UsbDescriptors.interfaceInterpretation(0xFF, 0x00, 0x00)
                .startsWith("Vendor-specific"),
        )
    }

    @Test
    fun `deviceInterpretation summarises interface mix and limits`() {
        val flashDrive = UsbDescriptors.deviceInterpretation(
            listOf(Triple(0x08, 0x06, 0x50)),
        )
        assert(flashDrive.contains("Bulk-Only"))
        assert(flashDrive.contains("mount storage"))

        val empty = UsbDescriptors.deviceInterpretation(emptyList())
        assertEquals("No interfaces exposed.", empty)

        // CDC serial must carry the honest Android limitation, not a promise
        val serial = UsbDescriptors.deviceInterpretation(
            listOf(Triple(0x02, 0x02, 0x01), Triple(0x0A, 0x00, 0x00)),
        )
        assert(serial.contains("does not provide a serial console"))
    }
}
