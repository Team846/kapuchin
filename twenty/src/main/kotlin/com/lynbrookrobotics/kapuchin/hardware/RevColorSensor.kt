package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.wpilibj.*
import info.kunalsheth.units.generated.*

import kotlin.math.sqrt
import kotlin.reflect.jvm.internal.impl.resolve.constants.LongValue

/**
 * RevColorSensor
 * Senses colors
 */
class RevColorSensor(i2cPort: I2C.Port, sensor_address: Int) {
    val MAIN_CTRL = 0x0
    val GREEN = 0x0D // register for green ASL, next 9 registers are the color registers
    val color_sensor: I2C // the sensor address for this particular sensor is 0x52

    init {
        color_sensor = I2C(i2cPort, sensor_address)
        color_sensor.write(MAIN_CTRL, 1 shl 2 or (1 shl 1)) // turns on RGB MODE and LS MODE (light sensor mode)
    }

    val colors = arrayOf(
//            doubleArrayOf(209.25, 228.5, 42.05),
//            doubleArrayOf(228.25, 88.2, 7.35),
//            doubleArrayOf(113.1, 228.5, 7.35),
//            doubleArrayOf(83.75, 228.5, 151.85)
        doubleArrayOf(16042.66, 24234.66, 566.13),
        doubleArrayOf(11605.33, 8260.26, 3413.33),
        doubleArrayOf(3891.2, 11195.73, 3822.93),
        doubleArrayOf(6621.86, 13789.86, 10854.4)
    )

    val color_names = arrayOf("yellow", "red", "green", "blue")

    // hopefully this works
    fun to20Bit(a: UInt, b: UInt, c: UInt): UInt {
        return (a shl 12 or (b shl 4) or c)
    }

    fun closestColor(red: Double, green: Double, blue: Double): String {
        var dist = java.lang.Double.MAX_VALUE
        var small  = ""
        for (i in 0..3) {
            val dr = red - colors[i][0]
            val dg = green - colors[i][1]
            val db = blue - colors[i][2]
            val curr = sqrt(dr * dr + dg * dg + db * db)
            if (curr < dist) {
                small = color_names[i]
                dist = curr
            }
        }
        return small
    }

    fun run() {
        var count = 0;
        while (true) {
            val raw = ByteArray(9)
            color_sensor.read(GREEN, 9, raw)
            val rawRed = to20Bit(raw[6].toUInt(), raw[7].toUInt(), raw[8].toUInt())
            val rawGreen = to20Bit(raw[0].toUInt(), raw[1].toUInt(), raw[2].toUInt())
            val rawBlue = to20Bit(raw[3].toUInt(), raw[4].toUInt(), raw[5].toUInt())
//            println(rawRed)
//            println(rawGreen)
//            println(rawBlue)
            println("Closest color: ${closestColor(rawRed.toDouble(), rawGreen.toDouble(), rawBlue.toDouble())}")
            Timer.delay(0.5); // currently delaying for 0.5 seconds, change this to higher frequency later...
        }
    }


}

