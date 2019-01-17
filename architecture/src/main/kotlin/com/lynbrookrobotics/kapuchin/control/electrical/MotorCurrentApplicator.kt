package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Applies current to a motor
 *
 * Controls acceleration. Prevents control code from damaging motors and/or causing brownouts.
 *
 * @author Kunal
 * @see motorCurrentLimiter
 *
 * @param maxVoltage motor's nominal voltage, often 12V. must be greater than zero.
 * @param freeSpeed motor's top speed. Must be greater than zero.
 * @param stall motor's stall current. Must be greater than zero.
 */
fun <S : Quan<S>> motorCurrentApplicator(
        maxVoltage: V, freeSpeed: S,
        stall: I
): (S, I) -> V {

    val windings = maxVoltage / stall

    return fun(speed: S, target: I): V {
        val emf = speed / freeSpeed * maxVoltage
        return target * windings + emf
    }
}