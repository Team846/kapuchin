package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class ClimberHooksComponent(hardware: ClimberHooksHardware) : Component<ClimberHooksComponent, ClimberHooksHardware, Boolean>(hardware) {
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