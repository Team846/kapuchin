package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.electrical.RampRateLimiter
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

/**
 * Calculates a derivative
 *
 * Takes the slope of the last two inputs
 *
 * @authors Kunal
 * @see RampRateLimiter
 *
 * @param Q type of input
 * @param DQDT derivative of input
 *
 * @param div UOM proof (just pass in `::div`)
 * @param x1 starting time
 * @param y1 initial value
 */
class Differentiator<Q, DQDT>(
        private val div: (Q, T) -> DQDT,
        private var x1: Time,
        private var y1: Q
) : (Time, Q) -> DQDT

        where Q : Quan<Q>,
              DQDT : Quan<DQDT> {

    override fun invoke(x2: Time, y2: Q) = div(
            y2 - y1, x2 - x1
    ).also {
        x1 = x2
        y1 = y2
    }
}