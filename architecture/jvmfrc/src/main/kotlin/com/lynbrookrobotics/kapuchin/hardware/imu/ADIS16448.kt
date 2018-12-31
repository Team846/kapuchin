package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector
import com.lynbrookrobotics.kapuchin.hardware.SPI
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.AngularVelocity
import info.kunalsheth.units.generated.Degree
import info.kunalsheth.units.generated.DegreePerSecond
import java.nio.ByteBuffer


/**
 * An interface for communicating with the ADIS16448 IMU.
 */
class ADIS16448(private val spi: SPI, updatePeriod: Double = 1.0 / 3000000): DigitalGyro(updatePeriod) {
    // List of register addresses on the IMU
    private object Registers {
        // Sample Period
        val SMPL_PRD: IMURegister = IMURegister(0x36)
        // Sensor data
        val SENS_AVG: IMURegister = IMURegister(0x38)
        // Misc. Control (resetting)
        val MSC_CTRL: IMURegister = IMURegister(0x34)
        // Product ID
        val PROD_ID: IMURegister = IMURegister(0x56)
    }

    // Private object used to store private variables
    private object ADIS16448Protocol {
        const val X_GYRO_VEL: Byte = 0x04 // try 0x12
        const val Y_GYRO_VEL: Byte = 0x06 // try 0x16
        const val Z_GYRO_VEL: Byte = 0x08 // try 0x1A
        const val X_DELTA_ANG: Byte = 0x42
        const val Y_DELTA_ANG: Byte = 0x46
        const val Z_DELTA_ANG: Byte = 0x4A
    }

    private object AngularVelocityConstants {
        // |Angular velocity| <= 1000 deg/sec
        const val DegreesPerSecondPerLSB1000: Double = 1.0 / 25.0
        // |Angular velocity| <= 500 deg/sec
        const val DegreesPerSecondPerLSB500: Double = 1.0 / 50.0
        // |Angular velocity| <= 250 deg/sec
        const val DegreesPerSecondPerLSB250: Double = 1.0 / 100.0
    }

    init {
        spi.setClockRate((1 / updatePeriod).toInt())
        spi.setMSBFirst()
        spi.setSampleDataOnFalling()
        spi.setClockActiveLow()
        spi.setChipSelectActiveLow()

        // Checks whether or not the IMU connected is the ADIS16448
        if (Registers.PROD_ID.read(spi) != 16448) {
            throw IllegalStateException("The device in the MXP port is not an ADIS16448 IMU")
        }
        // Saves the com.lynbrookrobotics.kapuchin.hardware.SPI being used (16448) to the various registers
        Registers.SMPL_PRD.write(1, spi) // 1 means use default period
        Registers.MSC_CTRL.write(4, spi) // 4 is reset command
        Registers.SENS_AVG.write(Integer.parseInt("10000000000", 2), spi) // Creates an empty queue of size 1024 bits
    }

    // Creates ByteBuffer of sie 2 for inputs and outputs
    private val outBuffer: ByteBuffer = ByteBuffer.allocateDirect(2)
    private val inBuffer: ByteBuffer = ByteBuffer.allocateDirect(2)
    private var firstRun = true

    /**
     * Returns data from register as short (16 bit integer)
     * @param register register- hex
     * @return short
     */
    private fun readGyroRegister(register: Byte): Short {
        outBuffer.put(0, register) // Request data from register
        outBuffer.put(1, 0.toByte()) // Second byte must be 0
        spi.write(outBuffer, 2) // Outputs 2 elements to spi

        inBuffer.clear()
        // inBuffer already defined so it does not need to be created
        // Reads 2 bytes and puts them in inBuffer
        spi.read(firstRun, inBuffer, 2)

        if (firstRun) firstRun = false

        return inBuffer.short
    }

    /**
     * Gets the current gyro data from the IMU.
     * 2nd and 3rd parameters are null because accelerometer and magneto data not used.
     * @return IMUValue
     */
    fun currentData(): IMUValue {
        val gyro: UomVector<AngularVelocity> = UomVector(
                DegreePerSecond(readGyroRegister(ADIS16448Protocol.X_GYRO_VEL)),
                DegreePerSecond(readGyroRegister(ADIS16448Protocol.Y_GYRO_VEL)),
                DegreePerSecond(readGyroRegister(ADIS16448Protocol.Z_GYRO_VEL))
        ).times(AngularVelocityConstants.DegreesPerSecondPerLSB1000) // when trying other registers, remove multiplying factor
        val dAngle: UomVector<Angle> = UomVector(
                Degree(readGyroRegister(ADIS16448Protocol.X_DELTA_ANG)),
                Degree(readGyroRegister(ADIS16448Protocol.Y_DELTA_ANG)),
                Degree(readGyroRegister(ADIS16448Protocol.Z_DELTA_ANG))
        )
        return IMUValue(gyro, dAngle)
    }

    override fun retrieveVelocity(): UomVector<AngularVelocity> {
        return currentData().gyro!!
    }

    override fun retrieveDeltaAngle(): UomVector<Angle> {
        return currentData().dAngle!!
    }
}