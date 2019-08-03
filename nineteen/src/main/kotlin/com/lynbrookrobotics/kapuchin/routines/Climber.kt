package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

suspend fun Climber.spin(target: DutyCycle) = startRoutine("Spin") {
    controller { target }
}