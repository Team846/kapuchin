package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*

private fun <Q, SQDT> integrator(
    p: (Q, `*`, T) -> SQDT,
    x1: Time, y1: Q,
    nextArea: (x1: Time, y1: Q, x2: Time, y2: Q) -> SQDT
): (Time, Q) -> SQDT

        where SQDT : Quan<SQDT>,
              Q : Quan<Q> {

    var x1 = x1
    var y1 = y1
    var sum = p(y1, `*`, x1).new(0.0)

    return fun(x2: Time, y2: Q): SQDT {
        sum += nextArea(x1, y1, x2, y2)

        x1 = x2
        y1 = y2

        return sum
    }
}

/**
 * Calculates an integral
 *
 * Adds up trapezoids since creation
 *
 * @author Kunal
 * @see finiteIntegrator
 *
 * @param Q type of input
 * @param SQDT integral of input
 *
 * @param p UOM proof (just pass in `::p`)
 * @param x1 starting time
 * @param y1 initial value
 */
fun <Q, SQDT> infiniteIntegrator(
    p: (Q, `*`, T) -> SQDT,
    x1: Time, y1: Q
): (Time, Q) -> SQDT

        where SQDT : Quan<SQDT>,
              Q : Quan<Q> {

    return integrator(p, x1, y1) { x1, y1, x2, y2 ->
        p(
            (y1 + y2) / 2, `*`, (x2 - x1)
        )
    }
}

/**
 * Calculates an integral
 *
 * Adds up the last `falloff` trapezoids
 *
 * @author Kunal
 * @see infiniteIntegrator
 *
 * @param Q type of input
 * @param SQDT integral of input
 *
 * @param p UOM proof (just pass in `::p`)
 * @param x1 starting time
 * @param y1 initial value
 * @param falloff number of trapezoids to look back when accumulating
 */
fun <Q, SQDT> finiteIntegrator(
    p: (Q, `*`, T) -> SQDT,
    x1: Time, y1: Q,
    falloff: Int
): (Time, Q) -> SQDT

        where SQDT : Quan<SQDT>,
              Q : Quan<Q> {

    val delayed = delay<SQDT>(falloff)
    val zero = p(y1, `*`, x1) * 0

    return integrator(p, x1, y1) { x1, y1, x2, y2 ->
        val area = p(
            (y1 + y2) / 2, `*`, (x2 - x1)
        )

        area - (delayed(area) ?: zero)
    }
}