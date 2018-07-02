package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.milli

class DriverHardware : SubsystemHardware<DriverHardware, Nothing>() {
    override val subsystemName = "Driver"
    override val priority = Priority.RealTime
    override val period = 20.milli(::Second)
    override val syncThreshold = 3.milli(::Second)

    val operatorPort by pref(1)
    val operatorStick by hardw { Joystick(operatorPort) }

    val driverPort by pref(0)
    val driverStick by hardw { Joystick(driverPort) }

    val wheelPort by pref(2)
    val driverWheel by hardw { Joystick(wheelPort) }
}