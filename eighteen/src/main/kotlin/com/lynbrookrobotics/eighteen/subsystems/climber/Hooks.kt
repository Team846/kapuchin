package com.lynbrookrobotics.eighteen.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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