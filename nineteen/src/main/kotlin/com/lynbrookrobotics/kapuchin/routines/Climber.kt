package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import info.kunalsheth.units.generated.*

suspend fun ClimberComponent.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { target }
}