package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.IntakePivotState.*
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
    var spin: Job? = null
    try{
        intakePivot?.set(Down)
        roller = launch { collectorRollers?.spin(electrical, collectorRollers.CollectSpeed) }
        freeze()

    }
    finally{
        roller?.cancel()
        spin = launch { carousel?.spin(electrical, carousel.carouselspeed) }
        delay(1.Second)
        spin?.cancel()
        intakePivot?.set(Up)

    }
}