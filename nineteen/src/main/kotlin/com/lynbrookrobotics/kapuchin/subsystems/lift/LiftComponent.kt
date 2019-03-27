package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectCargo by pref(6, Inch)
    val collectPanel by pref(2.24, Inch)
    val collectPanelStroke by pref(7.75, Inch)
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

    private val fallbackValue = PercentOutput(0.Percent)
    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { fallbackValue }

    private var lastReverseSoftLimit = Integer.MAX_VALUE
    private var lastForwardSoftLimit = Integer.MIN_VALUE
    override fun LiftHardware.output(value: OffloadedOutput) {

        lazyOutput(value)

//        val current = position.optimizedRead(currentTime, 0.Second).y
//
//        val range = unionizeAndFindClosestRange(LiftState.legalRanges(), current, (Int.MIN_VALUE + 1).Inch)
//
//        if (range.start - range.endInclusive != 0.Inch) {
//            val reverseSoftLimit = conversions.native.native(range.start).toInt()
//            if (reverseSoftLimit != lastReverseSoftLimit) {
//                lastReverseSoftLimit = reverseSoftLimit
//                esc.configReverseSoftLimitThreshold(reverseSoftLimit)
//            }
//
//            val forwardSoftLimit = conversions.native.native(range.endInclusive).toInt()
//            if (forwardSoftLimit != lastForwardSoftLimit) {
//                lastForwardSoftLimit = forwardSoftLimit
//                esc.configForwardSoftLimitThreshold(forwardSoftLimit)
//            }
//            lazyOutput(value)
//        } else if (Safeties.log) {
//            log(Warning) { "No legal states found" }
//        }
    }
}
