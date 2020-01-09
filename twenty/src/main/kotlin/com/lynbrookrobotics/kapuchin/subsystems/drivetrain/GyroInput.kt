package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import info.kunalsheth.units.generated.*

data class GyroInput(val angle: Angle, val velocity: AngularVelocity, val acceleration: AngularAcceleration)