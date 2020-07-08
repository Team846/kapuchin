package com.lynbrookrobotics.kapuchin.hardware.tickstoserial

inline class TicksToSerialValue(private val data: Int) {

    val left: Int
        get() {
            val left = data ushr 4 and 0x0F
            val absvl = left and 0b0111
            val signl = left and 0b1000 ushr 3
            return if (signl == 1) absvl else -absvl
        }

    val right: Int
        get() {
            val right = data ushr 0 and 0x0F
            val absvr = right and 0b0111
            val signr = right and 0b1000 ushr 3
            return if (signr == 1) absvr else -absvr
        }

    operator fun component1() = left
    operator fun component2() = right
}