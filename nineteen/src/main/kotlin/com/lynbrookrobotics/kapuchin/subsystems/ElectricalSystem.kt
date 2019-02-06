package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.data.stampSince
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.Minute
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.math.milli

class ElectricalSystemHardware : SubsystemHardware<ElectricalSystemHardware, Nothing>() {
    override val name = "Electrical System"
    override val priority = Priority.Lowest
    override val period = 3.Minute
    override val syncThreshold = 5.milli(Second)

    // val pdp by hardw { PowerDistributionPanel() }
    val batteryVoltage = sensor { RobotController.getBatteryVoltage().Volt stampSince it }
}