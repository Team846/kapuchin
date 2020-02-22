package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import info.kunalsheth.units.generated.*

suspend fun IntakeSliderComponent.set(target: IntakeSliderState) = startRoutine("Set") {
    controller { target }
}

suspend fun IntakeRollersComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}
