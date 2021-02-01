package com.lynbrookrobotics.eighteen.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClampComponent(hardware: ClampHardware) : Component<ClampComponent, ClampHardware, Boolean>(hardware, EventLoop) {
    override val fallbackController: ClampComponent.(Time) -> Boolean = { false }

    override fun ClampHardware.output(value: Boolean) {
        solenoid.set(value)
    }
}

class ClampHardware : SubsystemHardware<ClampHardware, ClampComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val name = "Collector Clamp"

    val solenoidPort by pref(2)
    val solenoid by hardw { Solenoid(solenoidPort) }
}