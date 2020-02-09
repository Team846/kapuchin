package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {
    choreography {

    }
}

// TODO deploy climber
// TODO undeploy
// TODO extend winch
// TODO pull in winch
// TODO engage brake
// TODO unengage brake