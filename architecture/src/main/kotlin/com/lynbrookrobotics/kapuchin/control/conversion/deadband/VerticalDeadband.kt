package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.control.div

class VerticalDeadband<Q : Quan<Q>>(val yIntercept: Q, val max: Q) : (Q) -> Q {

    private val slope = (max - yIntercept) / max

    override fun invoke(input: Q): Q = when {
        input.isPositive -> input * slope + yIntercept
        input.isNegative -> input * slope - yIntercept
        else -> input * 0
    }
}