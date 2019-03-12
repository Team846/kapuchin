package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.*

fun trapezoidalMotionProfile(
        deceleration: Acceleration,
        topSpeed: Velocity
): (Length) -> Velocity {

    // cad18/trunk/Users/Kunal Sheth/trapezoidal-motion-profile.gcx
    return fun(error: Length) = (
            topSpeed.abs min v(deceleration, 0.FootPerSecond, error).abs
            ) * error.signum
}