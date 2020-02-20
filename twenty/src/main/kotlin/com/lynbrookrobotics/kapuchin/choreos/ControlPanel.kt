package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import kotlinx.coroutines.coroutineScope

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {

    val extend by operator.extendControlPanel.readEagerly().withoutStamps
    val stage2 by operator.controlPanelStage2.readEagerly().withoutStamps
    val stage3 by operator.controlPanelStage3.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { extend } to choreography { controlPanelPivot?.set(Up) ?: freeze() },
                { stage2 } to choreography { controlPanelSpinner?.spinStage2(electrical) ?: freeze() },
                { stage3 } to choreography { TODO("Control Panel Stage 3") }
        )
    }
}