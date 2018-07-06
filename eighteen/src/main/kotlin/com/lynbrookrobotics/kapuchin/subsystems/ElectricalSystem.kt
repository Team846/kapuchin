package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.stampSince
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, Nothing>() {
    override val subsystemName = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 1.milli(::Second)

    val pdp by hardw { PowerDistributionPanel() }

    val batteryVoltage = sensor { RobotController.getBatteryVoltage().Volt stampSince it }

    val rollersPdpPort by pref {
        val left by pref(0)
        val right by pref(1)
        ({ TwoSided(left, right) })
    }
    val rollersInputCurrent = sensor {
        TwoSided(pdp.getCurrent(rollersPdpPort.left).Ampere, pdp.getCurrent(rollersPdpPort.right).Ampere) stampWith it
    }
}