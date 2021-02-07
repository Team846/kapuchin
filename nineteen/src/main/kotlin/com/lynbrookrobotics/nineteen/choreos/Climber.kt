package com.lynbrookrobotics.nineteen.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.nineteen.Subsystems
import com.lynbrookrobotics.nineteen.routines.openLoop
import com.lynbrookrobotics.nineteen.routines.rainbow
import com.lynbrookrobotics.nineteen.routines.spin
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.climberTeleop() = startChoreo("Climber teleop") {

    val unleashTheCobra by operator.unleashTheCobra.readEagerly().withoutStamps
    val oShitSnekGoBack by operator.oShitSnekGoBack.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { unleashTheCobra } to choreography { unleashTheCobra() },
                { oShitSnekGoBack } to choreography { climber?.spin(-climber.maxOutput / 2) ?: freeze() }
        )
    }
}

suspend fun Subsystems.unleashTheCobra() = coroutineScope {
    launch {
        delay(0.5.Second)
        scope.launch { leds?.rainbow() }
        drivetrain.openLoop(30.Percent)
    }
    launch { leds?.rainbow() }
    climber?.spin(climber.maxOutput) ?: freeze()
}