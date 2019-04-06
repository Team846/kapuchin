package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun Subsystems.climberTeleop() = startChoreo("Climber teleop") {

    val unleashTheCobra by operator.unleashTheCobra.readEagerly().withoutStamps
    val oShitSnekGoBack by operator.oShitSnekGoBack.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { unleashTheCobra } to choreography {
                    launch {
                        delay(0.5.Second)
                        scope.launch { leds?.rainbow() }
                        drivetrain.openLoop(30.Percent)
                    }
                    launch { leds?.rainbow() }
                    climber?.spin(climber.maxOutput) ?: freeze()
                },
                { oShitSnekGoBack } to choreography { climber?.spin(-climber.maxOutput / 2) ?: freeze() }
        )
    }
}
