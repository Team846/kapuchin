package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.*
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.math.sqrt

// https://www.khanacademy.org/science/physics/one-dimensional-motion/kinematic-formulas/a/what-are-the-kinematic-formulas

fun v(a: Acceleration, v0: Velocity, t: Time): Velocity = v0 + a * t
fun dx(v: Velocity, v0: Velocity, t: Time): Length = t * (v + v0) / 2
fun dx(a: Acceleration, v0: Velocity, t: Time): Length = (v0 * t) + (a * t * t / 2)
fun v2(a: Acceleration, v0: Velocity, dx: Length): `L²⋅T⁻²` = (v0 * v0) + (a * dx * 2)
fun v(a: Acceleration, v0: Velocity, dx: Length): Velocity {
    val v2 = v2(a, v0, dx).siValue
    return Velocity(sqrt(v2.absoluteValue) * v2.sign)
}