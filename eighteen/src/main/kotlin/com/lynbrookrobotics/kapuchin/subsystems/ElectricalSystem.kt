package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampSince
import com.lynbrookrobotics.kapuchin.delegates.sensors.eagerSensor
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*

class ElectricalSystemComponent(hardware: ElectricalSystemHardware) : Component<ElectricalSystemComponent, ElectricalSystemHardware, Unit>(hardware) {

    val batterySensor = eagerSensor {
        RobotController.getBatteryVoltage().Volt stampSince it
    }

    override val fallbackController: ElectricalSystemComponent.(Time) -> Unit = {}
    override fun ElectricalSystemHardware.output(value: Unit) {}
}

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, ElectricalSystemComponent>() {
    override val subsystemName = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 1.milli(::Second)
}