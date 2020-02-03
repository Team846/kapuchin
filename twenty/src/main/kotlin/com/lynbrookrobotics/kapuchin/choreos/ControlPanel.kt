package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {


    choreography {
        runWhenever(

        )
    }
}

suspend fun Subsystems.spin() = coroutineScope {
    try {
        launch {
            controlPanelPivot?.set(ControlPanelPivotState.Up)


        }
    } finally {

    }
}