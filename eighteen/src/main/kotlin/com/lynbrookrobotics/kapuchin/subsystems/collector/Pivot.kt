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

class PivotComponent(hardware: PivotHardware) : Component<PivotComponent, PivotHardware, Boolean>(hardware, EventLoop) {
    override val fallbackController: PivotComponent.(Time) -> Boolean = { false }

    override fun PivotHardware.output(value: Boolean) {
        solenoid.set(value)
    }
}

class PivotHardware : SubsystemHardware<PivotHardware, PivotComponent>() {
    override val priority get() = TODO()
    override val period get() = TODO()
    override val syncThreshold = 50.milli(Second)
    override val subsystemName = "Collector Pivot"

    val solenoidPort by pref(1)
    val solenoid by hardw { Solenoid(solenoidPort) }
}