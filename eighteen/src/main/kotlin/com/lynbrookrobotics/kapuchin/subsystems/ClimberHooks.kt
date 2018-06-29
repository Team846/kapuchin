package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.hardware.readEagerly
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class ClimberHooksComponent(hardware: ClimberHooksHardware, driver: DriverHardware) : Component<ClimberHooksComponent, ClimberHooksHardware, Boolean>(hardware) {
    val deployButton1 by pref(11)
    val deployButton2 by pref(14)
    val isTriggered by readEagerly {
        (driver.operatorStick.getRawButton(deployButton1) && driver.operatorStick.getRawButton(deployButton2)) stampWith it
    }

    override val fallbackController: ClimberHooksComponent.(Time) -> Boolean = { false }
    override fun ClimberHooksHardware.output(value: Boolean) = solenoidValve.set(value)
}

class ClimberHooksHardware : SubsystemHardware<ClimberHooksHardware, ClimberHooksComponent>() {
    override val priority = Priority.Low
    override val period = 100.milli(::Second)
    override val syncThreshold = 50.milli(::Second)
    override val subsystemName = "Climber Hooks"

    val solenoidPort by pref(0)
    val solenoidValve by hardw { Solenoid(solenoidPort) }
}