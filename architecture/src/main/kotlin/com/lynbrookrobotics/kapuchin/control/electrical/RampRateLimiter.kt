package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

/**
 * Prevents values from increasing too fast
 *
 * Intended to prevent excess current draw when controlling motors without feedback.
 *
 * @author Kunal
 * @see MotorCurrentLimiter
 * @see Differentiator
 *
 * @param Q type of input and output
 * @param D derivative of input
 *
 * @param div UOM proof (just pass in `::div`)
 * @param times UOM proof (just pass in `::times`)
 * @param x1 start time
 * @param y1 initial value
 * @param limit function returning max ramp rate
 */
fun <Q, D> rampRateLimiter(
        div: (Q, T) -> D,
        times: (D, T) -> Q,
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
        val ramp = div(dv, dt)

        return when {
            ramp > rampRate -> y1 + times(rampRate, dt)
            ramp < -rampRate -> y1 - times(rampRate, dt)
            else -> y2
        }.also {
            x1 = x2
            y1 = y2
        }
    }
}