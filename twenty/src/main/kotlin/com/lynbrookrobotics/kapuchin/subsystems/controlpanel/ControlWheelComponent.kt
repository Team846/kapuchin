package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

data class ControlPanelOutput(val upstate : Boolean, val wheelspeed : DutyCycle  ) {

}
class ControlWheelComponent (hardware: ControlWheelHardware) : Component<ControlWheelComponent, ControlWheelHardware, DutyCycle>(hardware) {
    override val fallbackController: ControlWheelComponent.(Time) -> DutyCycle ={0.Percent}

    override fun ControlWheelHardware.output(value: DutyCycle) {

    }

}