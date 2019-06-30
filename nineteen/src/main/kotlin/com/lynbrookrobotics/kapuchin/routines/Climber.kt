package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

fun Climber.set(target: DutyCycle) = newRoutine("Set") {
    controller { target }
}