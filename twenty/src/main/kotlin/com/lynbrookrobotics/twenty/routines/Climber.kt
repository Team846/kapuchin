package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.twenty.subsystems.climber.*
import info.kunalsheth.units.generated.*

suspend fun ClimberWinchComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { ClimberWinchOutput.Running(PercentOutput(hardware.escConfig, target)) }
}

suspend fun ClimberPivotComponent.set(target: ClimberPivotState) = startRoutine("Set") {
    controller { target }
}
