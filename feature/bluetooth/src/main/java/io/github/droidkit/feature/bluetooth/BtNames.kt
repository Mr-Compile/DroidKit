package io.github.droidkit.feature.bluetooth

/** Name maps for BluetoothDevice type/bond constants — pure, unit-testable. */
object BtNames {
    fun deviceType(type: Int): String = when (type) {
        0 -> "Unknown"
        1 -> "Classic"
        2 -> "LE"
        3 -> "Dual (Classic + LE)"
        else -> "Unknown"
    }

    fun bondState(state: Int): String = when (state) {
        10 -> "Not bonded"
        11 -> "Bonding"
        12 -> "Bonded"
        else -> "Unknown"
    }
}
