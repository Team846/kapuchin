package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

class EncoderConversion(ticks: Each, perRevolution: Angle) {
    private val anglePerTick = perRevolution / ticks

    fun ticks(x: AngularAbsement) = ticks(x / Second) * Second
    fun ticks(x: Radian): Double = (x / anglePerTick).Each
    fun ticks(x: AngularVelocity) = ticks(x * Second) * Hertz
    fun ticks(x: AngularAcceleration) = ticks(x * Second) * Hertz

    fun angle(x: Time): AngularAbsement = angle((x / Second).Each) * Second
    fun angle(x: Double): Radian = x * anglePerTick
    fun angle(x: Frequency) = angle((x * Second).Each) * Hertz
    fun angle(x: `T⁻²`) = angle(x * Second) * Hertz
}