package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*

/**
 * Prevents values from increasing too fast
 *
 * Intended to prevent excess current draw when controlling motors without feedback.
 *
 * @author Kunal
 * @see motorCurrentLimiter
 * @see differentiator
 *
 * @param Q type of input and output
 * @param D derivative of input
 *
 * @param p1 UOM proof (just pass in `::p`)
 * @param p2 UOM proof (just pass in `::p`)
 * @param x1 start time
 * @param y1 initial value
 * @param limit function returning max ramp rate
 */
fun <Q, D> rampRateLimiter(
        p1: (Q, `÷`, T) -> D,
        p2: (D, `*`, T) -> Q,
        x1: Time, y1: Q,
        limit: (Time) -> D
): (Time, Q) -> Q

        where Q : Quan<Q>,
              D : Quan<D> {

    var x1 = x1
    var y1 = y1

    return fun(x2: Time, y2: Q): Q {
        val rampRate = limit(x2)

        val dt = x2 - x1
        val dv = y2 - y1
        val ramp = p1(dv, `÷`, dt)

        return when {
            ramp > rampRate -> y1 + p2(rampRate, `*`, dt)
            ramp < -rampRate -> y1 - p2(rampRate, `*`, dt)
            else -> y2
        }.also {
            x1 = x2
            y1 = y2
        }
    }
}