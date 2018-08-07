package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.control.div
import com.lynbrookrobotics.kapuchin.control.withToleranceOf

class HorizontalDeadband<Q : Quan<Q>>(val xIntercept: Q, val max: Q) : (Q) -> Q {

    private val slope = max / (max - xIntercept)

    override fun invoke(input: Q): Q = when {
        input > xIntercept -> (input - xIntercept) * slope
        input < -xIntercept -> (input + xIntercept) * slope
        else -> input * 0
    }
}