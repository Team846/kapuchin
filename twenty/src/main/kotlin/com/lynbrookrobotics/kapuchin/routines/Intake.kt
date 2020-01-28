package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import info.kunalsheth.units.generated.*

suspend fun IntakePivotComponent.set(target: IntakePivotState) = startRoutine("Set") {
    controller { target }
}

suspend fun CollectorRollersComponent.spin(speed: ElectricCurrent) = startRoutine("Spin") {
    controller {
        CurrentOutput(hardware.escConfig, speed)
    }
}

suspend fun CollectorRollersComponent.set(state: OffloadedOutput) = startRoutine("Set") {
    controller { state }
}
