package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*

/**
 * Start climber
 */
suspend fun unleashTheCobra(
        climber: ClimberComponent
) = startChoreo("Unleash the cobra") {

    choreography {
        climber.spin(climber.maxOutput)
    }
}
