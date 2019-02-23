package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import kotlinx.coroutines.isActive

/**
 * Start climber
 */
suspend fun unleashTheCobra(
        climber: ClimberComponent,
        oper: OperatorHardware
) = startChoreo("Unleash the cobra") {

    val unleashTheCobra by oper.unleashTheCobra.readEagerly().withoutStamps

    choreography {
        whenever({ unleashTheCobra }) {
            runWhile({ unleashTheCobra }) {
                climber.spin(climber.maxOutput)
            }
        }
    }
}
