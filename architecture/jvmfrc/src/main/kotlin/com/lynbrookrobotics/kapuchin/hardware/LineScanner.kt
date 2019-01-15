package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.hal.SerialPortJNI
import edu.wpi.first.wpilibj.SerialPort
import java.io.Closeable

class LineScanner(
        val port: SerialPort.Port
) : Closeable {

    val device = SerialPort(115200, port)

    private val receiveBuffer = ByteArray(128)
    private val sendBuffer = byteArrayOf(100)

    var exposure: UByte
        get() = sendBuffer[0].toUByte()
        set(value) { sendBuffer[0] = value.toByte() }

    operator fun invoke(): ByteArray {
        val bytesToRead = receiveBuffer.size

        if (device.bytesReceived > bytesToRead) {
            val bytesToDrain = device.bytesReceived / bytesToRead * bytesToRead
            val drainBuffer =
                    if (bytesToDrain < receiveBuffer.size) receiveBuffer
                    else ByteArray(bytesToDrain)
            SerialPortJNI.serialRead(port.value.toByte(), drainBuffer, bytesToDrain)
            System.err.println("device.bytesReceived returned $bytesToDrain, expected ≤ ${receiveBuffer.size}")
        }

        val bytesToWrite = sendBuffer.size
        SerialPortJNI.serialWrite(port.value.toByte(), sendBuffer, bytesToWrite)
                .takeIf { it != bytesToWrite }
                ?.also { System.err.println("SerialPortJNI.serialWrite returned $it, expected $bytesToWrite") }

        SerialPortJNI.serialRead(port.value.toByte(), receiveBuffer, bytesToRead)
                .takeIf { it != bytesToRead }
                ?.also { System.err.println("SerialPortJNI.serialRead returned $it, expected $bytesToRead") }

        return receiveBuffer
    }

    override fun close() = device.close()
}