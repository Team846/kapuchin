package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Limits the current applied to a motor
 *
 * Prevents control code from damaging motors and/or causing brownouts. Also limits acceleration.
 *
 * @author Kunal
 * @see outsideThresholdChecker
 * @see rampRateLimiter
 *
 * @param maxVoltage motor's nominal voltage, often 12V. must be greater than zero.
 * @param freeSpeed motor's top speed. Must be greater than zero.
 * @param stall motor's stall current. Must be greater than zero.
 * @param limit current limit. Must be greater than zero.
 */
fun <S : Quan<S>> motorCurrentLimiter(
    maxVoltage: V, freeSpeed: S,
    stall: I, limit: I
): (S, V) -> V {

    val windings = maxVoltage / stall

    return fun(speed: S, target: V): V {
        val emf = speed / freeSpeed * maxVoltage

        val expectedCurrent = (target - emf) / windings

        return when {
            expectedCurrent > limit -> limit * windings + emf
            expectedCurrent < -limit -> -(limit * windings - emf)
            else -> target
        }
    }
}