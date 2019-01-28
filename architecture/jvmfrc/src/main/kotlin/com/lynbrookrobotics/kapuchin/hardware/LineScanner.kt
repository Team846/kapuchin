package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.wpilibj.I2C
import java.io.Closeable
import java.io.IOException

class LineScanner(
        port: I2C.Port = I2C.Port.kOnboard,
        deviceAddress: Int = 10
) : Closeable {

    val device = I2C(port, deviceAddress)

    init {
        if (device.addressOnly()) throw IOException("Could not connect to I2C device with address $deviceAddress on port $port")
    }

    val resolution = 128

    private val receiveBuffer = ByteArray(resolution)
    private val sendBuffer = byteArrayOf(100)

    operator fun invoke(exposure: UByte = sendBuffer[0].toUByte()): ByteArray {
        sendBuffer[0] = exposure.toByte()

        if (device.transaction(sendBuffer, 1, receiveBuffer, resolution))
            System.err.println("I2C.transaction aborted")

        return receiveBuffer
    }

    override fun close() = device.close()
}