package com.lynbrookrobotics.eighteen.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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
    override val name = "Collector Pivot"

    val solenoidPort by pref(1)
    val solenoid by hardw { Solenoid(solenoidPort) }
}