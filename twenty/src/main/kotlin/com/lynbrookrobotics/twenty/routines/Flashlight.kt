package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState

suspend fun FlashlightComponent.set(state: FlashlightState) = startRoutine("Set") {
    controller { state }
}

suspend fun FlashlightComponent.strobe() = startRoutine("Strobe") {
    var state = FlashlightState.On
    controller {
        state = if (state == FlashlightState.On) FlashlightState.Off else FlashlightState.On
        state
    }
}