package com.lynbrookrobotics.nineteen.routines

import com.lynbrookrobotics.nineteen.subsystems.ClimberComponent
import info.kunalsheth.units.generated.*

suspend fun ClimberComponent.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { target }
}