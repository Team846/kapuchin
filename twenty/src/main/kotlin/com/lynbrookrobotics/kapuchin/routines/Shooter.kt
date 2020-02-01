package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(target) }
}

suspend fun HoodComponent.set(target: HoodState) = startRoutine("Set") {
    controller { target }
}

suspend fun FlywheelComponent.set(target: Velocity) = startRoutine("Set") {
    controller { VelocityOutput(target) }
}

suspend fun TurretComponent.set(target: Position) = startRoutine("Set") {
    controller { PositionOutput(target) }
}