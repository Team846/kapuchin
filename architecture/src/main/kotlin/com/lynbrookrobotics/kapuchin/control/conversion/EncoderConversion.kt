package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.*

class EncoderConversion(ticks: Int, perRevolution: Angle) {
    private val anglePerTick = perRevolution / ticks.Each

    fun ticks(x: AngularAbsement) = ticks(x / t) * t
    fun ticks(x: Angle): Double = x / anglePerTick
    fun ticks(x: AngularVelocity) = ticks(x * t) * Hertz
    fun ticks(x: AngularAcceleration) = ticks(x * t) * Hertz

    fun angle(x: Time): AngularAbsement = angle((x / t).Each) * t
    fun angle(x: Double): Angle = x * anglePerTick
    fun angle(x: Frequency) = angle((x * t).Each) * Hertz
    fun angle(x: `T⁻²`) = angle(x * t) * Hertz
}