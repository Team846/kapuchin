package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

class EncoderConversion(ticks: Tick, perRevolution: Angle) {
    private val ticksPerRevolution = ticks / perRevolution

    fun ticks(x: AngularAbsement) = ticks(x / t) * t
    fun ticks(x: Radian): Tick = (x / 1.Turn) * ticksPerRevolution
    fun ticks(x: AngularVelocity) = ticks(x * t) / t
    fun ticks(x: AngularAcceleration) = ticks(x * t) / t

    fun angle(x: Time): AngularAbsement = t * angle((x / t))
    fun angle(x: Tick): Radian = (x / ticksPerRevolution) * 1.Turn
    fun angle(x: Frequency) = angle((x * t)) / t
    fun angle(x: `T⁻²`) = angle(x * t) / t

    companion object {
        private val t = 1.Second
    }
}