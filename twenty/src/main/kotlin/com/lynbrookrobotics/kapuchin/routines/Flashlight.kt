package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.FlashlightState.*
import edu.wpi.first.wpilibj.Relay.Value.kOff
import edu.wpi.first.wpilibj.Relay.Value.kOn

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