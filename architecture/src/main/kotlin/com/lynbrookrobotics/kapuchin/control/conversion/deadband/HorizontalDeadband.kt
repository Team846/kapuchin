package com.lynbrookrobotics.kapuchin.control.conversion.deadband

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*

/**
 * Maps inputs close to zero to zero
 *
 * Intended for joystick input to prevent drifting
 *
 * @author Alvyn, Kunal
 * @see verticalDeadband
 *
 * @param Q type of input and output
 * @param xIntercept maximum input that will still be mapped to zero. Must be greater than zero.
 * @param max value at which the input is equal to output. Must be greater than zero.
 */
fun <Q : Quan<Q>> horizontalDeadband(xIntercept: Q, max: Q): (Q) -> Q {

    val slope = max / (max - xIntercept)

    return fun(input: Q): Q = when {
        input > xIntercept -> (input - xIntercept) * slope
        input < -xIntercept -> (input + xIntercept) * slope
        else -> input * 0
    }
}