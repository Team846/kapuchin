package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Safeties.legalRanges
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectHeight by pref(0, Inch)
    val collectGroundPanel by pref(15, Inch)

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

    private var lastReverseSoftLimit = Integer.MAX_VALUE
    private var lastForwardSoftLimit = Integer.MIN_VALUE
    override fun LiftHardware.output(value: OffloadedOutput) {
        val legal = legalRanges()
        val current = position.optimizedRead(currentTime, 0.Second).y

        val closest = legal.firstOrNull { current in it } ?: legal.minBy {
            val center = avg(it.start, it.endInclusive)
            (current - center).abs
        }

        if (closest != null) {
            val reverseSoftLimit = conversions.native.native(closest.start).toInt()
            if (reverseSoftLimit != lastReverseSoftLimit) esc.configReverseSoftLimitThreshold(reverseSoftLimit)

            val forwardSoftLimit = conversions.native.native(closest.endInclusive).toInt()
            if (forwardSoftLimit != lastForwardSoftLimit) esc.configReverseSoftLimitThreshold(reverseSoftLimit)
        } else if (Safeties.log)
            log(Warning) { "No legal states found" }

        lazyOutput(value)
    }
}