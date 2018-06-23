package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.Acceleration
import info.kunalsheth.units.generated.Length
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.Velocity
import kotlin.math.sqrt

// https://www.khanacademy.org/science/physics/one-dimensional-motion/kinematic-formulas/a/what-are-the-kinematic-formulas

fun v(a: Acceleration, v0: Velocity, t: Time) = v0 + a * t
fun dx(v: Velocity, v0: Velocity, t: Time) = t * (v + v0) / 2
fun dx(a: Acceleration, v0: Velocity, t: Time) = (v0 * t) + (a * t * t / 2)
fun v(a: Acceleration, v0: Velocity, dx: Length) = Velocity(sqrt(((v0 * v0) + (a * dx * 2)).siValue))