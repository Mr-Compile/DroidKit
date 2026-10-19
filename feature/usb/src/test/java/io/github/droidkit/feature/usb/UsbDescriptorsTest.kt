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
}
