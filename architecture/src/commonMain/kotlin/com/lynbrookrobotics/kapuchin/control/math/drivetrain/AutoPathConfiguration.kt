package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import info.kunalsheth.units.generated.*

/**
 * Configuration values for trajectory generation and following of a recorded path.
 *
 * @property name name of the path file on the robot.
 * @property reverse follow path forward or in reverse.
 * @property endingVelocity the velocity the trajectory should end at.
 * @property speedFactor robot max speed and acceleration proportion.
 * @property maxAccel robot max acceleration.
 * @property maxAccel robot max deceleration.
 * @property percentMaxOmega robot max omega proportion.
 * @property maxExtrap max follower extrapolation distance.
 * @property extrapK follower extrapolation distance formula constant.
 *
 * @author Andy
 */
data class AutoPathConfiguration(
    val name: String,
    val reverse: Boolean,
    val endingVelocity: Velocity,
    val speedFactor: DutyCycle,
    val maxAccel: Acceleration,
    val maxDecel: Acceleration,
    val percentMaxOmega: DutyCycle,
    val maxExtrap: Length,
    val extrapK: Double,
)