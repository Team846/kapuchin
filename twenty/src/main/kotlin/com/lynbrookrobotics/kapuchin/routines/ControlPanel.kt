package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*

suspend fun ControlPanelPivotComponent.set(state: ControlPanelPivotState) = startRoutine( "Set") {
    controller { state }
}

suspend fun ControlPanelSpinnerComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}
