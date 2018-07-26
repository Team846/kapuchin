package com.lynbrookrobotics.kapuchin.control

class Hysteresis<Q : Quan<Q>>(val range: ClosedRange<Q>) : (Q) -> Boolean {
    private var isTriggered = false

    override fun invoke(value: Q) = when {
        isTriggered && value < range.start -> false
        !isTriggered && value > range.endInclusive -> true
        else -> isTriggered
    }.also { isTriggered = it }
}