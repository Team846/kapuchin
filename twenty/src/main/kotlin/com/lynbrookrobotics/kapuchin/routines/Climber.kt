package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberWinchOutput.*
import info.kunalsheth.units.generated.*

suspend fun ClimberWinchComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { Running(PercentOutput(hardware.escConfig, target)) }
}

suspend fun ClimberPivotComponent.set(target: ClimberPivotState) = startRoutine("Set") {
    controller { target }
}
