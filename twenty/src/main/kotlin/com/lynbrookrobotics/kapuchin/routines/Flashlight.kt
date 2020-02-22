package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import edu.wpi.first.wpilibj.Relay.Value.kOff
import edu.wpi.first.wpilibj.Relay.Value.kOn

suspend fun FlashlightComponent.set(state: FlashlightState) = startRoutine("Set") {
    controller { state }
}

suspend fun FlashlightComponent.strobe() = startRoutine("Strobe") {
    var state = kOn
    controller {
        state = if (state == kOn) kOff else kOn
        state
    }
}