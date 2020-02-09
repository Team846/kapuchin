package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import info.kunalsheth.units.generated.*

suspend fun ClimberChodeComponent.set(target: ClimberChodeState) = startRoutine("Set") {
    controller { target }
}

suspend fun ClimberWinchComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun ClimberPivotComponent.set(target: ClimberPivotState) = startRoutine("Set") {
    controller { target }
}