package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*

suspend fun Subsystems.teletop() = startChoreo("Teletop") {
    val wacking = operator.wacker.readOptimized.withoutStamps
    choreography {
        runWhenever(
                { wacking } to ::flipUp
        )
    }
}

suspend fun Subsystems.flipUp() {
    controlPanelPivot?.set(Up)
}

suspend fun Subsystems.flipDown() {
    controlPanelPivot?.set(Down)
}

suspend fun Subsystems.spinSpinner() {
    controlWheel?.set(controlWheel.motorSpeed)
}