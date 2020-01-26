package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import kotlinx.coroutines.Job
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
    var storage: Job? = null
    var omni: Job? = null
    try{
        roller = launch {  }
    }
    finally{

    }
}