package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class HooksComponent(hardware: HooksHardware) : Component<HooksComponent, HooksHardware, Boolean>(hardware, EventLoop) {
    override val fallbackController: HooksComponent.(Time) -> Boolean = { false }
    override fun HooksHardware.output(value: Boolean) = solenoidValve.set(value)
}

class HooksHardware : SubsystemHardware<HooksHardware, HooksComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val name = "Climber Hooks"

    val solenoidPort by pref(0)
    val solenoidValve by hardw { Solenoid(solenoidPort) }
}