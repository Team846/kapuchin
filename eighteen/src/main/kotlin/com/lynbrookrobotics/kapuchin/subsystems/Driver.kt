package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.math.NumVector
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.hardware.dsl.hardw
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class DriverComponent(hardware: DriverHardware) : Component<DriverComponent, DriverHardware, Unit>(hardware) {
    override val fallbackController: DriverComponent.(Time) -> Unit = {}
    override fun DriverHardware.output(value: Unit) {}
}

class DriverHardware : SubsystemHardware<DriverHardware, DriverComponent>() {
    override val subsystemName = "Driver"
    override val priority = Priority.RealTime
    override val period = 20.milli(::Second)
    override val syncThreshold = 3.milli(::Second)

    val operatorPort by pref(1)
    val operatorStick by hardw { Joystick(operatorPort) }.readWithEventLoop {
        NumVector(x, y, z) stampWith it
    }

    val driverPort by pref(0)
    val driverStick by hardw { Joystick(driverPort) }.readWithEventLoop {
        NumVector(x, y, z) stampWith it
    }

    val wheelPort by pref(2)
    val driverWheel by hardw { Joystick(wheelPort) }.readWithEventLoop {
        NumVector(x, y, z) stampWith it
    }
}