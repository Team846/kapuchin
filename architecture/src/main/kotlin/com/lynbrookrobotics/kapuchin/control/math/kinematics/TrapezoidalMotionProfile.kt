package com.lynbrookrobotics.kapuchin.control.math.kinematics

import info.kunalsheth.units.generated.*

data class TrapezoidalMotionProfile(
        val distance: Length,
        val startingSpeed: Velocity,
        val acceleration: Acceleration,
        val topSpeed: Velocity,
        val deceleration: Acceleration = acceleration,
        val endingSpeed: Velocity = 0.FootPerSecond
) : (Length) -> Velocity {

    private val absDistance = distance.abs
    private val absStartingSpeed = startingSpeed.abs
    private val absAcceleration = acceleration.abs
    private val absTopSpeed = topSpeed.abs
    private val absDeceleration = deceleration.abs
    private val absEndingSpeed = endingSpeed.abs
    private val direction = distance.signum

    // cad18/trunk/Users/Kunal Sheth/trapezoidal-motion-profile.gcx
    override fun invoke(dx: Length): Velocity {
        val signedDx = dx * direction

        return when {
            signedDx < 0.Foot -> absStartingSpeed
            signedDx > absDistance -> absEndingSpeed
            else -> v(absAcceleration, absStartingSpeed, signedDx) min
                    absTopSpeed min
                    v(absDeceleration, absEndingSpeed, absDistance - signedDx)
        } * direction
    }
}