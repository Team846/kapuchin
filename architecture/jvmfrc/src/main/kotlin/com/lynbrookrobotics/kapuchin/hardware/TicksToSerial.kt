package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.hal.SerialPortJNI
import java.io.Closeable
import java.io.Flushable

class TicksToSerial(
        val port: SerialPort.Port
) : Flushable, Closeable {

    val device = SerialPort(115200, port)

    operator fun invoke() = sequence {
        val buffer = ByteArray(1)
        repeat(device.bytesReceived) {
            // DO NOT USE WPILIB's `read(..)` FUNCTION
            // it creates 1 or 2 new byte arrays every time you call it...
            SerialPortJNI.serialRead(port.value.toByte(), buffer, 1)
            yield(parse(buffer[0].toInt()))
        }
    }

    private fun parse(data: Int): TwoSided<Int> {
        val left = data shr 4 and 0x0F
        val absvl = left and 0b0111
        val signl = left and 0b1000 ushr 3

        val right = data ushr 0 and 0x0F
        val absvr = right and 0b0111
        val signr = right and 0b1000 ushr 3

        return TwoSided(
                left = if (signl == 1) absvl else -absvl,
                right = if (signr == 1) absvr else -absvr
        )
    }

    override fun flush() {
        device.reset()
    }

    override fun close() {
        device.free()
    }
}