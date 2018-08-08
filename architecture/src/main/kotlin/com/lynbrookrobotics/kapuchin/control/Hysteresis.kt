package com.lynbrookrobotics.kapuchin.control

import info.kunalsheth.units.generated.Quan

class Hysteresis<Q : Quan<Q>>(val range: ClosedRange<Q>) : (Q) -> Boolean {
    private var isTriggered = false

    override fun invoke(value: Q) = when {
        isTriggered && value < range.start -> false
        !isTriggered && value > range.endInclusive -> true
        else -> isTriggered
    }.also { isTriggered = it }
}