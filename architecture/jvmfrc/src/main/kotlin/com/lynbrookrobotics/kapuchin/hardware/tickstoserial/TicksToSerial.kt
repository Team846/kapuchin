package com.lynbrookrobotics.kapuchin.hardware.tickstoserial

import edu.wpi.first.hal.SerialPortJNI
import edu.wpi.first.wpilibj.SerialPort
import java.io.Closeable

class TicksToSerial(
        val port: SerialPort.Port
) : Closeable {

    val device = SerialPort(115200, port).apply {
        setReadBufferSize(2000)
        setTimeout(0.01)
        setWriteBufferSize(1)
    }

    private val buffer = ByteArray(2000)

    operator fun invoke() = sequence {
        val recieved = device.bytesReceived

        val gotten = SerialPortJNI.serialRead(port.value.toByte(), buffer, recieved)

        repeat(gotten) { i ->
            yield(TicksToSerialValue(buffer[i].toInt()))
        }

        if(gotten > 0.8 * buffer.size) println("TicksToSerial buffer ≥ 80% full. (Received $gotten bytes)")
    }

    override fun close() {
        device.close()
    }
}