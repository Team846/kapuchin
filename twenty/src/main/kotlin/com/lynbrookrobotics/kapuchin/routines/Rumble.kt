package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

suspend fun RumbleComponent.set(target: Rumble) = startRoutine("Set") {
    controller { target }
}

suspend fun RumbleComponent.error(rate: Frequency) = startRoutine("Error") {
    controller {
        Rumble((currentTime * rate * 2).roundToInt(Each))
    }
}