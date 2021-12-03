package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.twenty.subsystems.driver.RumbleComponent
import info.kunalsheth.units.generated.*

suspend fun RumbleComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { target }
}