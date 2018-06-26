package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.Quan
import info.kunalsheth.units.generated.Time

class ThresholdChecker<Q : Quan<Q>>(val threshold: Q, val forDuration: Time) : (Time, Q) -> Boolean {

    private var start: Time? = null

    override fun invoke(x: Time, y: Q) =
            if (y > threshold) {
                if (start == null) start = x
                x - (start ?: x) > forDuration
            } else {
                start = null
                false
            }
}