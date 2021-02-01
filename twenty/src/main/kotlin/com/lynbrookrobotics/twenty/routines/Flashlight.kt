package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.*

suspend fun FlashlightComponent.set(state: FlashlightState) = startRoutine("Set") {
    controller { state }
}

suspend fun FlashlightComponent.strobe() = startRoutine("Strobe") {
    var state = On
    controller {
        state = if (state == On) Off else On
        state
    }
}