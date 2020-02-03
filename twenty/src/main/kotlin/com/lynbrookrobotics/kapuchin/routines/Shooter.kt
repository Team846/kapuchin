package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*

suspend fun ShooterComponent.set(state: OffloadedOutput) = startRoutine("Set") {
    controller { state }
}

suspend fun TurretComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun ShooterHoodComponent.set(state: ShooterHoodState) = startRoutine("Set") {
    controller { state }
}