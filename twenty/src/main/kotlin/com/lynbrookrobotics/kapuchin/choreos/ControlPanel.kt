package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun Subsystems.controlpanelTeleop() = startChoreo("Control Panel") {

    choreography{
        val wacking by operator.wacker.readEagerly().withoutStamps


        runWhenever(
                { wacking } to choreography { flip() }
        )
    }
}


suspend fun Subsystems.flip() = supervisorScope{
    var wheel: Job? = null
    try {
        controlPanelPivot?.set(Up)
        delay(0.2.Second)
        wheel = launch { controlPanelSpinner?.set(controlPanelSpinner.motorSpeed)  }


        freeze()
    }
    finally {
        wheel?.cancel()
        controlPanelPivot?.set(Down)

    }
}

