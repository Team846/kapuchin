package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import kotlinx.coroutines.isActive

/**
 * Start climber
 */
suspend fun Subsystems.climberTeleop() = startChoreo("Unleash the cobra") {

    val unleashTheCobra by operator.unleashTheCobra.readEagerly().withoutStamps
    val oShitSnekGoBack by operator.oShitSnekGoBack.readEagerly().withoutStamps

    choreography {
        runWhenever(
            { unleashTheCobra } to choreography { climber?.spin(climber.maxOutput) ?: freeze() },
            { oShitSnekGoBack } to choreography { climber?.spin(-climber.maxOutput) ?: freeze() }
        )
    }
}
