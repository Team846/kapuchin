package com.lynbrookrobotics.kapuchin.control.math.integration

import com.lynbrookrobotics.kapuchin.control.math.Delay
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

/**
 * Calculates an integral
 *
 * Adds up the last `falloff` trapezoids
 *
 * @author Kunal
 * @see InfiniteIntegrator
 *
 * @param Q type of input
 * @param SQDT integral of input
 *
 * @param times UOM proof (just pass in `::times`)
 * @param x1 starting time
 * @param y1 initial value
 *
 * @property falloff number of trapezoids to look back when accumulating
 */
class FiniteIntegrator<Q, SQDT>(
        times: (Q, T) -> SQDT,
        x1: Time, y1: Q,
        val falloff: Int
) : InfiniteIntegrator<Q, SQDT>(times, x1, y1)

        where SQDT : Quan<SQDT>,
              Q : Quan<Q> {

    private val delayed = Delay<SQDT>(falloff)
    private val zero = sum * 0

    override fun invoke(x2: Time, y2: Q): SQDT {
        val trapezoid = recentTrapezoid(x2, y2)
        val delayed = delayed(trapezoid) ?: zero

        sum += trapezoid - delayed
        return sum
    }
}