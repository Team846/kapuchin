package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import info.kunalsheth.units.generated.*

suspend fun ControlPanelPivotComponent.set(state: ControlPanelPivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun ControlPanelSpinnerComponent.spinStage2(electrical: ElectricalSystemHardware) = startRoutine("Stage 2") {
    val controlPanelAngle by hardware.controlPanelAngle.readEagerly.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        if (controlPanelAngle < 4.Turn) {
            val error = 4.Turn - controlPanelAngle
            val voltage = kP * error

            voltageToDutyCycle(voltage, vBat)
        } else {
            0.Percent
        }
    }
}

suspend fun ControlPanelSpinnerComponent.spinStage3() = startRoutine("Stage 3") {
    val currentColorOrdinal by hardware.currentColorOrdinal.readEagerly.withoutStamps
    val targetColorOrdinal by hardware.targetColorOrdinal.readEagerly.withoutStamps
    controller {
        if (currentColorOrdinal == targetColorOrdinal) {
            100.Percent
        } else {
            0.Percent
        }
    }
}