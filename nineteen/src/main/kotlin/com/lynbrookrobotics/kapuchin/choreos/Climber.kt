package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*

/**
 * Start climber
 */
suspend fun Subsystems.climberTeleop() = startChoreo("Climber teleop") {

    val unleashTheCobra by operator.unleashTheCobra.readEagerly().withoutStamps
    val oShitSnekGoBack by operator.oShitSnekGoBack.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { unleashTheCobra } to choreography { climber?.spin(climber.maxOutput) ?: freeze() },
                { oShitSnekGoBack } to choreography { climber?.spin(-climber.maxOutput / 2) ?: freeze() }
        )
    }
}
