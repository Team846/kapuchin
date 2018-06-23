package com.lynbrookrobotics.kapuchin.subsystems.drivetrain.routines

import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.*

fun DrivetrainComponent.driveStraight(
        distance: Length,
        tolerance: Length,
        turnTolerance: Angle,
        withinToleranceFor: Time,
        maxAccel: Acceleration,
        maxSpeed: Velocity,
        maxDecel: Acceleration = maxAccel
): DrivetrainSubroutine {

}