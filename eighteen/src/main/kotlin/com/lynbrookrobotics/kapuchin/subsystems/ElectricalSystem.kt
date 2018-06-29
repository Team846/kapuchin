package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampSince
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, Nothing>() {
    override val subsystemName = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 1.milli(::Second)

    val batteryVoltage by hardw { }.readEagerly {
        RobotController.getBatteryVoltage().Volt stampSince it
    }
}