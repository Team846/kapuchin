package com.lynbrookrobotics.kapuchin.subsystems.drivetrain.routines

import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.Acceleration
import info.kunalsheth.units.generated.Length
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.Velocity

fun DrivetrainComponent.driveStraight(
        distance: Length,
        tolerance: Length,
        withinToleranceFor: Time,
        maxAccel: Acceleration,
        maxSpeed: Velocity,
        maxDecel: Acceleration = maxAccel
): DrivetrainSubroutine {

}