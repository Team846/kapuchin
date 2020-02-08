package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import info.kunalsheth.units.generated.*

suspend fun IntakePivotComponent.set(state: IntakePivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun IntakeRollersComponent.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { PercentOutput(hardware.escConfig, target) }
}



