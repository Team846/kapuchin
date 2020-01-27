package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import info.kunalsheth.units.generated.*

suspend fun ControlPanelSpinnerComponent.spin(electrical: ElectricalSystemHardware, Rollers: V) = startRoutine("spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        voltageToDutyCycle(Rollers, vBat)
    }
}

suspend fun ControlPanelSpinnerComponent.set(target: DutyCycle) = startRoutine("set") {
    controller { target }
}

suspend fun ControlPanelPivotComponent.set(target: ControlPanelPivotState) = startRoutine("Set") {
    controller { target }
}