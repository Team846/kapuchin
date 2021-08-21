package com.lynbrookrobotics.twenty.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ControlPanelPivotState(val output: Boolean) { Up(true), Down(false) }

class ControlPanelPivotComponent(hardware: ControlPanelPivotHardware) :
    Component<ControlPanelPivotComponent, ControlPanelPivotHardware, ControlPanelPivotState>(hardware,
        Subsystems.pneumaticTicker) {

    override val fallbackController: ControlPanelPivotComponent.(Time) -> ControlPanelPivotState =
        { ControlPanelPivotState.Down }

    override fun ControlPanelPivotHardware.output(value: ControlPanelPivotState) {
        pivotSolenoid.set(value.output)
    }
}

class ControlPanelPivotHardware : SubsystemHardware<ControlPanelPivotHardware, ControlPanelPivotComponent>() {
    override val period by Subsystems.sharedTickerTiming
    override val syncThreshold = 50.milli(Second)
    override val priority = Priority.Low
    override val name = "Control Panel Pivot"

    private val pivotSolenoidChannel = 2

    val pivotSolenoid by hardw { Solenoid(pivotSolenoidChannel) }
}