package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.ControlPanelPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*

enum class ControlPanelPivotState(val output: Boolean) { Up(true), Down(false) }

class ControlPanelPivotComponent(hardware: ControlPanelPivotHardware) : Component<ControlPanelPivotComponent, ControlPanelPivotHardware, ControlPanelPivotState>(hardware, pneumaticTicker) {

    override val fallbackController: ControlPanelPivotComponent.(Time) -> ControlPanelPivotState = { Down }

    override fun ControlPanelPivotHardware.output(value: ControlPanelPivotState) {
        pivotSolenoid.set(value.output)
    }
}

class ControlPanelPivotHardware : SubsystemHardware<ControlPanelPivotHardware, ControlPanelPivotComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Low
    override val name: String = "Control Panel Pivot"

    private val pivotSolenoidChannel = 2
    val pivotSolenoid by hardw { Solenoid(pivotSolenoidChannel) }
}