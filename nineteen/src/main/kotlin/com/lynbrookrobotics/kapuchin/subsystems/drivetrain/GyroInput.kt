package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.AngularAcceleration
import info.kunalsheth.units.generated.AngularVelocity

data class GyroInput(val angle: Angle, val velocity: AngularVelocity, val acceleration: AngularAcceleration)