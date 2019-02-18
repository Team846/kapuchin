package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.Subsystems
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectHeight by pref(0, Inch)

    val panelLowRocket by pref(19, Inch)
    val panelMidRocket by pref(47, Inch)
    val panelHighRocket by pref(75, Inch)

    private val panelCargoOffset by pref(-6, Inch)
    val cargoLowRocket get() = panelLowRocket + panelCargoOffset
    val cargoMidRocket get() = panelMidRocket + panelCargoOffset
    val cargoHighRocket get() = panelHighRocket + panelCargoOffset

    val kP by pref(12, Volt, 12, Inch)
    val kD by pref(0, Volt, 2, FootPerSecond)

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun LiftHardware.output(value: OffloadedOutput) = lazyOutput(value)
}