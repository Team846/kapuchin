package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.electrical.*
import info.kunalsheth.units.generated.*

/**
 * Calculates a derivative
 *
 * Takes the slope of the last two inputs
 *
 * @author Kunal
 * @see rampRateLimiter
 *
 * @param Q type of input
 * @param DQDT derivative of input
 *
 * @param p UOM proof (just pass in `::p`)
 * @param x1 starting time
 * @param y1 initial value
 */
fun <Q, DQDT> differentiator(
    p: (Q, `÷`, T) -> DQDT,
    x1: Time, y1: Q
): (Time, Q) -> DQDT

        where Q : Quan<Q>,
              DQDT : Quan<DQDT> {

    var x1 = x1
    var y1 = y1

    return fun(x2: Time, y2: Q) = p(
        (y2 - y1), `÷`, (x2 - x1)
    ).also {
        x1 = x2
        y1 = y2
    }
}