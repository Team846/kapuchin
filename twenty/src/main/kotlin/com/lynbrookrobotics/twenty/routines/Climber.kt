package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.twenty.subsystems.climber.*
import info.kunalsheth.units.generated.*

suspend fun ClimberWinchComponent.set(target: DutyCycle, ignoreLimit: Boolean = false) = startRoutine("Set") {
    controller {
        if (ignoreLimit) ClimberWinchOutput.RunningNoSafety(PercentOutput(hardware.escConfig, target))
        else ClimberWinchOutput.Running(PercentOutput(hardware.escConfig, target))
    }
}

suspend fun ClimberPivotComponent.set(target: ClimberPivotState) = startRoutine("Set") {
    controller { target }
}

