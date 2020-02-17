package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.controlPanelTeleop() = startChoreo("Control Panel Teleop") {

    val extend by operator.extendControlPanel.readEagerly().withoutStamps
    val stage2 by operator.controlPanelStage2.readEagerly().withoutStamps
    val stage3 by operator.controlPanelStage3.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { extend } to choreography { extendControlPanel() },
                { stage2 } to choreography { TODO("stage2") },
                { stage3 } to choreography { TODO("stage3") }
        )
    }
}

suspend fun Subsystems.extendControlPanel() = coroutineScope {
    controlPanelPivot?.set(Up)
    freeze()
}
