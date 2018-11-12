package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.Quan

/**
 * Maps inputs close to zero to zero
 *
 * Often used on joystick input to prevent drifting
 *
 * @author Alvyn, Kunal
 * @see [VerticalDeadband]
 *
 * @param[Q] the type of input and output
 * @param[xIntercept] must be greater than zero
 * @param[max] must be greater than zero
 *
 * @property[xIntercept] the maximum input that will still be mapped to zero
 * @property[max] the value at which the input is equal to output
 */
class HorizontalDeadband<Q : Quan<Q>>(val xIntercept: Q, val max: Q) : (Q) -> Q {

    private val slope = max / (max - xIntercept)

    override fun invoke(input: Q): Q = when {
        input > xIntercept -> (input - xIntercept) * slope
        input < -xIntercept -> (input + xIntercept) * slope
        else -> input * 0
    }
}