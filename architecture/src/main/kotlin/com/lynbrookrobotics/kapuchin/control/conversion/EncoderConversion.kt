package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

class EncoderConversion(val ticks: Tick, val perRevolution: Angle) {
    private val ticksPerRevolution = ticks / perRevolution

    fun ticks(x: AngularAbsement) = ticks(x / t) * t
    fun ticks(x: Radian): Tick = (x / fullTurn) * ticksPerRevolution
    fun ticks(x: AngularVelocity) = ticks(x * t) / t
    fun ticks(x: AngularAcceleration) = ticks(x * t) / t

    fun angle(x: Time): AngularAbsement = t * angle((x / t))
    fun angle(x: Tick): Radian = (x / ticksPerRevolution) * fullTurn
    fun angle(x: Frequency) = angle((x * t)) / t
    fun angle(x: `T⁻²`) = angle(x * t) / t

    companion object {
        protected val t = 1.Second
        protected val fullTurn: Angle = 360.Degree
    }
}