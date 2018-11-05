package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

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