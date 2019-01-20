package com.lynbrookrobotics.kapuchin.hardware

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.`±`
import info.kunalsheth.units.math.abs
import com.lynbrookrobotics.kapuchin.logging.withDecimals

/**
 * Converts Alps RDC80 analog readings into angles
 *
 * @author Kunal
 *
 * @param phaseShift the average difference between the a and b channels.
 * @param tolerance the approximate range of the phase shift.
 */
fun alpsRdc80(
        phaseShift: Dimensionless = 50.Percent,
        tolerance: Dimensionless = 10.Percent
): (Dimensionless, Dimensionless) -> Angle {

    val center = 50.Percent
    val safeRadius = 40.Percent
    val safeRange = center `±` safeRadius

    var readingA = true
    var readingB = true

    /**
     * @param a must be between 0 and 1 inclusive
     * @param b must be between 0 and 1 inclusive
     * @return potentiometer angle
     */
    return fun(a: Dimensionless, b: Dimensionless): Angle {
        val inPhaseB = (b + phaseShift) % 1.0.Each

        if (a !in safeRange) readingA = false
        if (b !in safeRange) readingB = false

        if (
                a in safeRange && b in safeRange &&
                a in inPhaseB `±` tolerance
        ) {
            readingA = true
            readingB = true
        }

        return when {
            readingA && readingB -> {
                val aWeight = safeRadius - abs(center - a)
                val bWeight = safeRadius - abs(center - b)
                ((a * aWeight) + (inPhaseB * bWeight)) / (aWeight + bWeight)
            }
            readingA -> a
            readingB -> inPhaseB
            else -> throw IllegalStateException("ALPS RDC80 Channels Out of Range. A=${a withDecimals 2}, B=${b withDecimals 2}")
        } * Turn
    }
}