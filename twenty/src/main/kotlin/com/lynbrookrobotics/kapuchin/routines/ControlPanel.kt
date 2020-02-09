package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import info.kunalsheth.units.generated.*

suspend fun ControlPanelPivotComponent.set(state: ControlPanelPivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun ControlPanelSpinnerComponent.spinStage2(target: OffloadedOutput) = startRoutine("Stage 2") {
    val controlPanelAngle by hardware.controlPanelAngle.readEagerly.withoutStamps
    controller {
        if (controlPanelAngle < 4.Turn) {
/*            var error = 4.Turn - controlPanelAngle
            val kP = 5
            var output: Percent = kP*error
            PercentOutput(hardware.escConfig, output.Percent)
            Todo: Finish Proportional Control
 */
            target
        } else {
            PercentOutput(hardware.escConfig, 0.Percent)
        }
    }
}

suspend fun ControlPanelSpinnerComponent.spinStage3(target: OffloadedOutput) = startRoutine("Stage 3") {
    val currentColor by hardware.currentColor.readEagerly.withoutStamps
    val gameData by hardware.gameData.readEagerly.withoutStamps
    controller {
        if (currentColor.toString() == gameData) {
            target
        } else {
            PercentOutput(hardware.escConfig, 0.Percent)
        }
    }
}