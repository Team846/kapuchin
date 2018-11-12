package com.lynbrookrobotics.kapuchin.control.math.integration

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

/**
 * Calculates an integral
 *
 * Adds up trapezoids since creation
 *
 * @authors Kunal
 * @see FiniteIntegrator
 *
 * @param Q type of input
 * @param SQDT integral of input
 *
 * @param times UOM proof (just pass in `::times`)
 * @param x1 starting time
 * @param y1 initial value
 */
open class InfiniteIntegrator<Q, SQDT>(
        private val times: (Q, T) -> SQDT,
        private var x1: Time,
        private var y1: Q
) : Integrator<Q, SQDT>

        where SQDT : Quan<SQDT>,
              Q : Quan<Q> {

    protected var sum = times(y1 * 0, x1)

    override fun invoke(x2: Time, y2: Q): SQDT {
        sum += recentTrapezoid(x2, y2)
        return sum
    }

    protected fun recentTrapezoid(x2: Time, y2: Q) = times(
            (y2 + y1) / 2, x2 - x1
    ).also {
        x1 = x2
        y1 = y2
    }
}