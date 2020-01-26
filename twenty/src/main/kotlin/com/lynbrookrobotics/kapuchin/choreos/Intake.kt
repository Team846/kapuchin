package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.IntakePneumaticState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun Subsystems.IntakeTeleop() = startChoreo("Intake Teleop") {

    choreography{

        runWhenever(


        )
    }
}


suspend fun Subsystems.Collect() = supervisorScope {
    var roller: Job? = null
    var carousel: Job? = null
    var omni: Job? = null
    try{
        roller = launch { collectorRollers?.spin(electrical, collectorRollers.CollectSpeed) }
        freeze()

    }
    finally{
        roller?.cancel()
        intakePneumatic?.set(Up)
        carousel = launch { storage?.spin(electrical, collectorRollers.CollectSpeed) }
        delay(1.Second)
        carousel?.cancel()

    }
}