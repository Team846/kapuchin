package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectCargo by pref(6, Inch)
    val collectPanel by pref(2.24, Inch)
    val collectGroundPanel by pref(0, Inch)

    val panelLowRocket by pref(4.24, Inch)
    val rocketLevelShift by pref(29, Inch)
    val panelMidRocket get() = panelLowRocket + rocketLevelShift
    val panelHighRocket get() = panelLowRocket + rocketLevelShift * 2

    private val panelCargoOffset by pref(-2.5, Inch)
    val cargoLowRocket get() = panelLowRocket + panelCargoOffset
    val cargoCargoShip by pref(17, Inch)
    val cargoMidRocket get() = panelMidRocket + panelCargoOffset
    val cargoHighRocket get() = panelHighRocket + panelCargoOffset

    val kP by pref(12, Volt, 12, Inch)
    val kD by pref(0, Volt, 2, FootPerSecond)

    val lopsidePeakOutput by pref(50, Percent)
    val lopsideRange by pref(3, Inch)

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    private var lastReverseSoftLimit = Integer.MAX_VALUE
    private var lastForwardSoftLimit = Integer.MIN_VALUE
    override fun LiftHardware.output(value: OffloadedOutput) {
        Subsystems.instance!!.collectorSlider?.let {
            val position = it.hardware.position.optimizedRead(currentTime, 0.Second).y
            if (position !in `±`(lopsideRange)) {
                +esc.configPeakOutputForward(lopsidePeakOutput.Each)
            } else {
                +esc.configPeakOutputForward(1.0)
            }
        }
        lazyOutput(value)
    }
}
