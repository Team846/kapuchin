package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.shooter.*

// TODO flywheel set to goal
// TODO flywheel set angular velocity

// TODO turret set position

suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}