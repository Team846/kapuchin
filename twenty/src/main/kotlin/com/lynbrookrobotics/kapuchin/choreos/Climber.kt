package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {


    choreography {
        runWhenever(

        )
    }
}

suspend fun Subsystems.climb() = coroutineScope {
    launch {
        climberPivot?.set(Deployed)
        delay(0.1.Second)
        climberWinch?.spin()
        freeze()
        
    }
}