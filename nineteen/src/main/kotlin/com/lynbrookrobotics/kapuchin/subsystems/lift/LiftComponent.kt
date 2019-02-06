package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    //diameter
    val ballHeight by pref(13, Inch)

    val collectHeight by pref(0, Inch)

    val minPosition by pref(0, Inch)

    //center
    val lowRocketHeight by pref(19, Inch)
    val midRocketHeight by pref(47, Inch)
    val highRocketHeight by pref(75, Inch)

    //center - offsetted by a constant
    private val k = 2
    val offsetLowRocketHeight by pref(19 + k, Inch)
    val offsetMidRocketHeight by pref(47 + k, Inch)
    val offsetHighRocketHeight by pref(75 + k, Inch)

    val positionGains by pref {
        val kP by pref(12, Volt, 8, Inch)
        ({ OffloadedPidGains(
                hardware.offloadedSettings.native(kP),
                0.0, 0.0, 0.0
        ) })
    }


    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun LiftHardware.output(value: OffloadedOutput) = lazyOutput(value)

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }

}