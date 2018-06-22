package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.math.NumVector
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.delegates.sensors.withEventLoopSensor
import com.lynbrookrobotics.kapuchin.hardware.dsl.hardw
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class DriverComponent(hardware: DriverHardware) : Component<DriverComponent, DriverHardware, Unit>(hardware) {

    val operatorAxis by withEventLoopSensor {
        NumVector(operatorStick.x, operatorStick.y, operatorStick.z) stampWith it
    }

    val driverAxis by withEventLoopSensor {
        NumVector(driverStick.x, driverStick.y, driverStick.z) stampWith it
    }

    val wheelAxis by withEventLoopSensor {
        NumVector(driverWheel.x, driverWheel.y, driverWheel.z) stampWith it
    }

    override val fallbackController: DriverComponent.(Time) -> Unit = {}
    override fun DriverHardware.output(value: Unit) {}
}

class DriverHardware : SubsystemHardware<DriverHardware, DriverComponent>() {
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