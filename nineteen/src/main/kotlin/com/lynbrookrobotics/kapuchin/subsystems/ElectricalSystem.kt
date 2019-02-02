package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, Nothing>() {
    override val name = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 5.milli(Second)

    // val pdp by hardw { PowerDistributionPanel() }
    val batteryVoltage = sensor { RobotController.getBatteryVoltage().Volt stampSince it }
}