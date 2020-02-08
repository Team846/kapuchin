package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*

suspend fun FlywheelComponent.set(target: Velocity) = startRoutine("Set") {
    controller { VelocityOutput(target) }
}

suspend fun HoodComponent.set(target: HoodState) = startRoutine("Set") {
    controller { target }
}


suspend fun TurretComponent.set(target: Angle) = startRoutine("Set") {
    controller { PositionOutput(target) }
}