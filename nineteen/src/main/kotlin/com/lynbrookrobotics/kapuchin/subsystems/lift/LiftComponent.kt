package com.lynbrookrobotics.kapuchin.subsystems.lift
import com.lynbrookrobotics.kapuchin.control.data.Gain
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware) {

    val positionGains by pref (12, Volt, 8, Inch)

    //diameter
    val ballHeight by pref(13, Inch)

    val collectHeight by pref(0, Inch)

    //center
    val cargoBayHeight by pref(19, Inch)
    val lowRocketHeight by pref(19, Inch)
    val midRocketHeight by pref(47, Inch)
    val highRocketHeight by pref(75, Inch)

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun LiftHardware.output(value: OffloadedOutput) = lazyOutput(value)

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }

}