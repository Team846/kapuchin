package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.Acceleration
import info.kunalsheth.units.generated.FootPerSecond
import info.kunalsheth.units.generated.Length
import info.kunalsheth.units.generated.Velocity

@Deprecated("TrapezoidalMotionProfile should output current, not velocity")
fun trapezoidalMotionProfile(
        distance: Length,
        startingSpeed: Velocity,
        acceleration: Acceleration,
        topSpeed: Velocity,
        deceleration: Acceleration = acceleration,
        endingSpeed: Velocity = 0.FootPerSecond
): (Length) -> Velocity {

    val absDistance = distance.abs
    val direction = distance.signum

    // cad18/trunk/Users/Kunal Sheth/trapezoidal-motion-profile.gcx
    return fun(dx: Length): Velocity {
        val signedDx = dx * direction

        return (v(acceleration, startingSpeed, signedDx) min
                topSpeed min
                v(deceleration, endingSpeed, absDistance - signedDx)
                ) * direction
    }
}