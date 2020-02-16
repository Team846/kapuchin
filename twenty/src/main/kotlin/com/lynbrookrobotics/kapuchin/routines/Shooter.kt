package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*

// TODO flywheel set to goal
// TODO flywheel set angular velocity

// TODO turret set position
suspend fun TurretComponent.set() = startRoutine("Set")
{
    val limelight by limelihardware.
    controller{
        val offset = limelightReading.tx

    }
}
suspend fun ShooterHoodComponent.set(target: ShooterHoodState) = startRoutine("Set") {
    controller { target }
}

suspend fun TurretComponent.zeroing() = startChoreo("Zero") {
    hardware.zero()
    choreography {
        while (hardware.isZeroed) delay(0.2.Second)
    }
}