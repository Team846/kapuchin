package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Safeties.legalRanges
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.TreeSet

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

        val current = position.optimizedRead(currentTime, 0.Second).y

        var currLeft: Length = (Int.MIN_VALUE + 1).Inch
        var currRight: Length = (Int.MIN_VALUE + 1).Inch

        legalRanges().sortedBy {
            it.start.Inch
        }.forEach {
            when {
                it.start <= currRight -> currRight = max(currRight, it.endInclusive)
                currLeft <= current && current <= currRight -> return
                (it.start - current).abs < (currRight - current).abs -> {
                    currLeft = it.start
                    currRight = it.endInclusive
                }
                else -> return
            }
        }

        if (currLeft - currRight != 0.Inch) {
            val reverseSoftLimit = conversions.native.native(currLeft).toInt()
            if (reverseSoftLimit != lastReverseSoftLimit) {
                lastReverseSoftLimit = reverseSoftLimit
                esc.configReverseSoftLimitThreshold(reverseSoftLimit)
            }

            val forwardSoftLimit = conversions.native.native(currRight).toInt()
            if (forwardSoftLimit != lastForwardSoftLimit) {
                lastForwardSoftLimit = forwardSoftLimit
                esc.configForwardSoftLimitThreshold(forwardSoftLimit)
            }
            lazyOutput(value)
        } else if (Safeties.log) {
            log(Warning) { "No legal states found" }
        }
    }
}
