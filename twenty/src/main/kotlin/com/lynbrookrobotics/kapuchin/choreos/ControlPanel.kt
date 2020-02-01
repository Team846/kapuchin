package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.control_panel.ControlPanelPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {


    choreography {
        runWhenever(

        )
    }
}

suspend fun Subsystems.spin() = coroutineScope {
    launch {
        controlPanelPivot?.set(Up)
        delay(0.1.Second)
        controlPanelSpinner?.spin()
        freeze()

    }
}