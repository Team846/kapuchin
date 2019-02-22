package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*

/**
 * Maps inputs too close to zero to a higher value
 *
 * Intended for control code to overcome friction and close small errors
 *
 * @author Alvyn, Kunal
 * @see horizontalDeadband
 *
 * @param Q type of input and output
 * @param yIntercept minimum output for any non-zero input. Must be greater than zero.
 * @param max value at which the input is equal to output. Must be greater than zero.
 */
fun <Q : Quan<Q>> verticalDeadband(yIntercept: Q, max: Q): (Q) -> Q {

    val slope = (max - yIntercept) / max

    return fun(input: Q): Q = when {
        input.isPositive -> input * slope + yIntercept
        input.isNegative -> input * slope - yIntercept
        else -> input * 0
    }
}