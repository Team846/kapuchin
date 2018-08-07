package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.control.div

class VerticalDeadband<Q : Quan<Q>>(val min: Q, val max: Q) : (Q) -> Q {

    private val slope = (max - min) / max

    override fun invoke(input: Q): Q = when {
        input.isPositive -> input * slope + min
        input.isNegative -> input * slope - min
        else -> input * 0
    }
}