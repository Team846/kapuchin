package com.lynbrookrobotics.kapuchin.hardware

import java.nio.ByteBuffer

/**
 * Class that outlines certain methods used by WPILIB's SPI object
 *
 * @author Nikash Walia
 */
abstract class SPI {
    abstract fun setClockRate(hz: Int)
    abstract fun setMSBFirst()
    abstract fun setSampleDataOnFalling()
    abstract fun setClockActiveLow()
    abstract fun setChipSelectActiveLow()
    abstract fun write(byte: ByteBuffer, size: Int): Int
    abstract fun read(initiate: Boolean, dataReceived: ByteBuffer, size: Int): Int
}