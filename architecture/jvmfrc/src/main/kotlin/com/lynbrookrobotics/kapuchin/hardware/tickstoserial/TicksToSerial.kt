package com.lynbrookrobotics.kapuchin.hardware.ticksToSerial

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import edu.wpi.first.hal.SerialPortJNI
import edu.wpi.first.wpilibj.SerialPort
import java.io.Closeable

class TicksToSerial(
        val port: SerialPort.Port
) : Closeable {

    val device = SerialPort(115200, port).apply {
        setReadBufferSize(5 * 1000)
        setTimeout(0.01)
        setWriteBufferSize(1)
    }

    operator fun invoke() = sequence {
        val buffer = ByteArray(1)
        repeat(device.bytesReceived) {
            // DO NOT USE WPILIB's `read(..)` FUNCTION
            // it creates 1 or 2 new byte arrays every time you call it...
            SerialPortJNI.serialRead(port.value.toByte(), buffer, 1)
            yield(TicksToSerialValue(buffer[0].toInt()))
        }
    }

    override fun close() {
        device.close()
    }
}