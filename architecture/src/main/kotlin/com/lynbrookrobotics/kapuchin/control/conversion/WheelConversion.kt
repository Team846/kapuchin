package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

class WheelConversion(val radius: Length) {
    fun angle(x: Absement): AngularAbsement = angle(x / t) * t
    fun angle(x: Length): Radian = x / radius
    fun angle(x: Velocity): AngularVelocity = angle(x * t) / t
    fun angle(x: Acceleration): AngularAcceleration = angle(x * t) / t

    fun length(x: AngularAbsement): Absement = length(x / t) * t
    fun length(x: Radian): Length = x * radius
    fun length(x: AngularVelocity): Velocity = length(x * t) / t
    fun length(x: AngularAcceleration): Acceleration = length(x * t) / t
}