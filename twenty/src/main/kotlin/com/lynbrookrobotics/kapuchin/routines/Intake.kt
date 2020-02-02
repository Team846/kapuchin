package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*

suspend fun IntakePivotComponent.set(state: IntakePivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun IntakeRollersComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}



