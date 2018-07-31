package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.Quan
import info.kunalsheth.units.generated.Time

class OutsideThresholdChecker<Q : Quan<Q>>(val safeRange: ClosedRange<Q>, val forDuration: Time) : (Time, Q) -> Boolean {

    private var start: Time? = null

    override fun invoke(x: Time, y: Q) =
            if (y !in safeRange) {
                if (start == null) start = x
                x - (start ?: x) > forDuration
            } else {
                start = null
                false
            }
}