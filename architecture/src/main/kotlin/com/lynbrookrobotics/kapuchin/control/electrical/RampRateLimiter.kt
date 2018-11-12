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
 *
 * @property limit function returning max ramp rate
 */
class RampRateLimiter<Q, D>(
        private val div: (Q, T) -> D,
        private val times: (D, T) -> Q,
        private var x1: Time,
        private var y1: Q,
        val limit: (Time) -> D
) : (Time, Q) -> Q

        where Q : Quan<Q>,
              D : Quan<D> {

    override operator fun invoke(x2: Time, y2: Q): Q {
        val limit = limit(x2)

        val dt = x2 - x1
        val dv = y2 - y1
        val ramp = div(dv, dt)

        return when {
            ramp > limit -> y1 + times(limit, dt)
            ramp < -limit -> y1 - times(limit, dt)
            else -> y2
        }.also {
            x1 = x2
            y1 = y2
        }
    }
}