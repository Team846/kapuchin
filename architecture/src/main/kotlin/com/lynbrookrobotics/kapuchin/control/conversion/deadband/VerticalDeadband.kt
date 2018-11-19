package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.Quan

/**
 * Maps inputs too close to zero to a higher value
 *
 * Intended for control code to overcome friction and close small errors
 *
 * @author Alvyn, Kunal
 * @see HorizontalDeadband
 *
 * @param Q type of input and output
 * @param yIntercept must be greater than zero
 * @param max must be greater than zero
 *
 * @property yIntercept minimum output for any non-zero input
 * @property max value at which the input is equal to output
 */
class VerticalDeadband<Q : Quan<Q>>(val yIntercept: Q, val max: Q) : (Q) -> Q {

    private val slope = (max - yIntercept) / max

    override fun invoke(input: Q): Q = when {
        input.isPositive -> input * slope + yIntercept
        input.isNegative -> input * slope - yIntercept
        else -> input * 0
    }
}