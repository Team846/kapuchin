package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {
    choreography {

    }
}

suspend fun Subsystems.stage2() = coroutineScope {
    var controlPanelPivotUp: Job? = null
    try {
        controlPanelPivotUp = launch { controlPanelPivot?.set(Up) }
        launch { controlPanelSpinner?.spinStage2(electrical) }
        freeze()
    } finally {
        controlPanelPivotUp?.cancel()
    }
}

suspend fun Subsystems.stage3() = coroutineScope {
    var controlPanelPivotUp: Job? = null
    try {
        controlPanelPivotUp = launch { controlPanelPivot?.set(Up) }
        launch { controlPanelSpinner?.spinStage3() }
        freeze()
    } finally {
        controlPanelPivotUp?.cancel()
    }
}
