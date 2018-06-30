package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampSince
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.Minute
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.generated.milli

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, Nothing>() {
    override val subsystemName = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 1.milli(::Second)

    val batteryVoltage by hardw { }.readEagerly {
        RobotController.getBatteryVoltage().Volt stampSince it
    }
}