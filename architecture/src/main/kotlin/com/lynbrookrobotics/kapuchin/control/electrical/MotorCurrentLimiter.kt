package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.data.Motor
import com.lynbrookrobotics.kapuchin.control.div
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
        spec: Motor, freeSpeed: S, limit: I
): (S, V) -> V {

    return fun(speed: S, target: V): V = spec.run {
        val emf = speed / freeSpeed * voltage

        val expectedCurrent = (target - emf) / windings

        when {
            expectedCurrent > limit -> limit * windings + emf
            expectedCurrent < -limit -> -(limit * windings - emf)
            else -> target
        }
    }
}