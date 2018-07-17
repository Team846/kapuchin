package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class ClampComponent(hardware: ClampHardware) : Component<ClampComponent, ClampHardware, Boolean>(hardware, EventLoop) {
    override val fallbackController: ClampComponent.(Time) -> Boolean = { false }

    override fun ClampHardware.output(value: Boolean) {
        solenoid.set(value)
    }
}

class ClampHardware : SubsystemHardware<ClampHardware, ClampComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(::Second)
    override val subsystemName = "Collector Pivot"

    val solenoidPort by pref(2)
    val solenoid by hardw { Solenoid(solenoidPort) }
}