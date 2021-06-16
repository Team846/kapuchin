package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.driver.Rumble
import com.lynbrookrobotics.twenty.subsystems.driver.RumbleComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun RumbleComponent.set(target: Rumble) = startRoutine("Set") {
    controller { target }
}

suspend fun RumbleComponent.error(rate: Frequency = 4.Hertz) = startRoutine("Error") {
    controller {
        Rumble((currentTime * rate * 2).roundToInt(Each).Each)
    }
}