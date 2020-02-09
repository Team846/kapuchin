package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*

suspend fun ShooterComponent.set(state: OffloadedOutput) = startRoutine("Set") {
    controller { state }
}

suspend fun TurretComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun ShooterHoodComponent.set(state: HoodState) = startRoutine("Set") {
    controller { state }
}