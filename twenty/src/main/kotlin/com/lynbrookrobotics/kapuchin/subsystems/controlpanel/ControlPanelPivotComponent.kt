package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*


class ControlPanelPivotComponent (hardware: ControlPanelPivotHardware) : Component<ControlPanelPivotComponent, ControlPanelPivotHardware, Boolean>(hardware) {
    override val fallbackController: ControlPanelPivotComponent.(Time) -> Boolean = {false}
    override fun ControlPanelPivotHardware.output(value: Boolean) {

    }


}