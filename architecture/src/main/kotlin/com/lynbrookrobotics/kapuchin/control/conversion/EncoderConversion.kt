package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

class EncoderConversion(ticks: Each, perRevolution: Angle) {
    private val anglePerTick = perRevolution / ticks

    fun ticks(x: AngularAbsement) = ticks(x / t) * t
    fun ticks(x: Radian): Double = (x / anglePerTick).Each
    fun ticks(x: AngularVelocity) = ticks(x * t) * Hertz
    fun ticks(x: AngularAcceleration) = ticks(x * t) * Hertz

    fun angle(x: Time): AngularAbsement = angle((x / t).Each) * t
    fun angle(x: Double): Radian = x * anglePerTick
    fun angle(x: Frequency) = angle((x * t).Each) * Hertz
    fun angle(x: `T⁻²`) = angle(x * t) * Hertz
}