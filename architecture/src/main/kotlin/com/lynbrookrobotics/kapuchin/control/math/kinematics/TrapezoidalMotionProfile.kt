package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.Acceleration
import info.kunalsheth.units.generated.FootPerSecond
import info.kunalsheth.units.generated.Length
import info.kunalsheth.units.generated.Velocity

@Deprecated("TrapezoidalMotionProfile should output current, not velocity")
data class TrapezoidalMotionProfile(
        val distance: Length,
        val startingSpeed: Velocity,
        val acceleration: Acceleration,
        val topSpeed: Velocity,
        val deceleration: Acceleration = acceleration,
        val endingSpeed: Velocity = 0.FootPerSecond
) : (Length) -> Velocity {

    private val absDistance = distance.abs
    private val direction = distance.signum

    // cad18/trunk/Users/Kunal Sheth/trapezoidal-motion-profile.gcx
    override fun invoke(dx: Length): Velocity {
        val signedDx = dx * direction

        return (v(acceleration, startingSpeed, signedDx) min
                topSpeed min
                v(deceleration, endingSpeed, absDistance - signedDx)
                ) * direction
    }
}