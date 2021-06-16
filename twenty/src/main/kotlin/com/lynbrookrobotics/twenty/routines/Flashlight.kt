package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.Off
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.On

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