package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


class ControlPanelPivotComponent(hardware: ControlPanelPivotHardware) : Component<ControlPanelPivotComponent, ControlPanelPivotHardware, ControlPanelPivotState>(hardware, Subsystems.pneumaticTicker) {
    override val fallbackController: ControlPanelPivotComponent.(Time) -> ControlPanelPivotState = { Down }

    override fun ControlPanelPivotHardware.output(value: ControlPanelPivotState) {
        solenoid.set(value.output)
    }


}

class ControlPanelPivotHardware : SubsystemHardware<ControlPanelPivotHardware, ControlPanelPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Control Panel Pivot"

    private val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }
}

enum class ControlPanelPivotState(val output: Boolean) { Up(true), Down(false) }