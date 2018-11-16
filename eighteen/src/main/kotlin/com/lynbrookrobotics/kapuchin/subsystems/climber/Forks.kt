package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class ForksComponent(hardware: ForksHardware) : Component<ForksComponent, ForksHardware, Boolean>(hardware, EventLoop) {
    override val fallbackController: ForksComponent.(Time) -> Boolean = { false }
    override fun ForksHardware.output(value: Boolean) = solenoidValve.set(value)
}

class ForksHardware : SubsystemHardware<ForksHardware, ForksComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val name = "Climber Forks"

    val solenoidPort by pref(3)
    val solenoidValve by hardw { Solenoid(solenoidPort) }
}