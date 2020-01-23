package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.wpilibj.I2C
import kotlin.math.sqrt

/**
 * RevColorSensor
 * Senses colors
 */
@ExperimentalUnsignedTypes
class RevColorSensor(i2cPort: I2C.Port, sensor_address: Int) {
    private val colorSensor = I2C(i2cPort, sensor_address) // the sensor address for this particular sensor is 0x52

    init {
        colorSensor.write(MAIN_CTRL, 1 shl 2 or (1 shl 1)) // turns on RGB MODE and LS MODE (light sensor mode)
    }

    private val colors = arrayOf(
            doubleArrayOf(16042.66, 24234.66, 566.13),
            doubleArrayOf(11605.33, 8260.26, 3413.33),
            doubleArrayOf(3891.2, 11195.73, 3822.93),
            doubleArrayOf(6621.86, 13789.86, 10854.4)
    )

    private val colorNames = arrayOf("yellow", "red", "green", "blue")

    fun to20Bit(a: UInt, b: UInt, c: UInt): UInt {
        return (a shl 12 or (b shl 4) or c)
    }

    private fun closestColor(red: Double, green: Double, blue: Double): String {
        val colorIndex = colors.indices.minBy { i ->
            val rDist = red - colors[i][0]
            val gDist = green - colors[i][1]
            val bDist = blue - colors[i][2]
            sqrt(rDist * rDist + gDist * gDist + bDist * bDist)
        }
        return colorNames[colorIndex!!]
    }

    fun getCurrentValue(): String {
        val raw = ByteArray(9)
        colorSensor.read(GREEN, 9, raw)
        val rawRed = to20Bit(raw[6].toUInt(), raw[7].toUInt(), raw[8].toUInt())
        val rawGreen = to20Bit(raw[0].toUInt(), raw[1].toUInt(), raw[2].toUInt())
        val rawBlue = to20Bit(raw[3].toUInt(), raw[4].toUInt(), raw[5].toUInt())
        return closestColor(rawRed.toDouble(), rawGreen.toDouble(), rawBlue.toDouble())
    }

    companion object {
        private const val GREEN = 0x0D // register for green ASL, next 9 registers are the color registers
        private const val MAIN_CTRL = 0x0
    }
}
