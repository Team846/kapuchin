package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.hal.SerialPortJNI
import edu.wpi.first.wpilibj.SerialPort
import java.io.Closeable

class LineScanner(
        val port: SerialPort.Port
) : Closeable {

    val device = SerialPort(115200, port).apply {
        setReadBufferSize(128)
        setTimeout(0.01)
        setWriteBufferSize(1)
    }

    val resolution = 128

    private val receiveBuffer = ByteArray(resolution)
    private val sendBuffer = byteArrayOf(100)

    operator fun invoke(exposure: UByte): ByteArray {
        val bytesToRead = receiveBuffer.size

        if (device.bytesReceived > bytesToRead) {
            val bytesToDrain = device.bytesReceived / bytesToRead * bytesToRead
            val drainBuffer =
                    if (bytesToDrain < receiveBuffer.size) receiveBuffer
                    else ByteArray(bytesToDrain)
            SerialPortJNI.serialRead(port.value.toByte(), drainBuffer, bytesToDrain)
            System.err.println("device.bytesReceived returned $bytesToDrain, expected ≤ ${receiveBuffer.size}")
        }

        sendBuffer[0] = exposure.toByte()

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