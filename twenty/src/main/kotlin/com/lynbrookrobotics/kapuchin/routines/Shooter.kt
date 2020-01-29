package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*

suspend fun FeederRollerComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun ShooterComponent.set(state: TwoSided<DutyCycle>) = startRoutine("Set") {
    controller { state }
}

suspend fun TurretComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}