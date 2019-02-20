package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

suspend fun ClimberComponent.spin(target: DutyCycle) {
    controller { target }
}