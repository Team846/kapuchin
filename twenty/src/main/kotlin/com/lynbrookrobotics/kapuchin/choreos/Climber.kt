package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {


    choreography {
        runWhenever(

        )
    }
}

suspend fun Subsystems.climb() = coroutineScope {
    var climbPivot: Job? = null
    try {
        climbPivot = launch { climberPivot?.set(Deployed) }
        delay(0.1.Second)
        launch { climberWinch?.spin(30.Percent) }
        freeze()
    } finally {
        withContext(NonCancellable) {
            climbPivot?.cancel()
        }
    }
}