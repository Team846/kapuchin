package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import kotlinx.coroutines.coroutineScope

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {


    choreography {
        runWhenever(

        )
    }
}

suspend fun Subsystems.firstStage() = coroutineScope {


}