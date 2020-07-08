package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.control.math.div
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Encoder conversion utility
 *
 * Utility functions for converting between encoder outputs and angles
 *
 * @author Kunal
 * @see OffloadedNativeConversion
 * @see GearTrain
 *
 * @param ticks number of encoder ticks in 1 `perRevolution`
 * @param perRevolution angle corresponding `ticks` encoder ticks
 */
class EncoderConversion(ticks: Int, perRevolution: Angle) {
    private val anglePerTick = perRevolution / ticks.Each

    fun ticks(x: AngularAbsement) = ticks(x / t) * t
    fun ticks(x: Angle): Double = x / anglePerTick
    fun ticks(x: AngularVelocity) = ticks(x * t) * Hertz
    fun ticks(x: AngularAcceleration) = ticks(x * t) * Hertz

    fun angle(x: Time): AngularAbsement = angle((x / t).Each) * t
    fun angle(x: Number): Angle = x.toDouble() * anglePerTick
    fun angle(x: Frequency) = angle((x * t).Each) * Hertz
    fun angle(x: `T⁻²`) = angle(x * t) * Hertz
}