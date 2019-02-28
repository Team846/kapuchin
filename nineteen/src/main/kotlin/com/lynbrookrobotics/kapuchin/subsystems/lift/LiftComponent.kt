package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectCargo by pref(5, Inch)
    val collectPanel by pref(10, Inch)
    val collectGroundPanel by pref(0, Inch)

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

        val range = unionizeAndFindClosestRange(LiftState.legalRanges(), current, (Int.MIN_VALUE + 1).Inch)

        if (range.start - range.endInclusive != 0.Inch) {
            val reverseSoftLimit = conversions.native.native(range.start).toInt()
            if (reverseSoftLimit != lastReverseSoftLimit) {
                lastReverseSoftLimit = reverseSoftLimit
                esc.configReverseSoftLimitThreshold(reverseSoftLimit)
            }

            val forwardSoftLimit = conversions.native.native(range.endInclusive).toInt()
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
