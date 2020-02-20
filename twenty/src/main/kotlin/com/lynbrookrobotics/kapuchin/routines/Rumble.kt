package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun RumbleComponent.set(target: Rumble) = startRoutine("Set") {
    controller { target }
}

suspend fun RumbleComponent.error(period: Time = 250.milli(Second)) = startRoutine("Error") {
    var prevTime = currentTime
    var state = true

    controller {
        if (currentTime - prevTime > period / 2) {
            state = !state
            prevTime = currentTime
        }
        if (state) Rumble(100.Percent) else Rumble(0.Percent)
    }
}