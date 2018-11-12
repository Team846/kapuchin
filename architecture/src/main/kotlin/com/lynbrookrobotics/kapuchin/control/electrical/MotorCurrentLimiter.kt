package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.*

/**
 * Limits the current applied to a motor
 *
 * Prevents control code from damaging motors and/or causing brownouts. Also limits acceleration.
 *
 * @authors Kunal
 * @see OutsideThresholdChecker
 * @see RampRateLimiter
 *
 * @param maxVoltage must be greater than zero
 * @param freeSpeed must be greater than zero
 * @param stall must be greater than zero
 * @param limit must be greater than zero
 *
 * @property maxVoltage motor's nominal voltage, often 12V
 * @property freeSpeed motor's top speed (look it up)
 * @property stall motors stall current (look it up)
 * @property limit current limit
 */
class MotorCurrentLimiter(
        val maxVoltage: V, val freeSpeed: AngularVelocity,
        val stall: I, val limit: I
) :
        (AngularVelocity, V) -> V,
        (V, I, V) -> V {

    private val motorR: ElectricalResistance = maxVoltage / stall

    override operator fun invoke(speed: AngularVelocity, target: V) = limit(
            emf = speed / freeSpeed * maxVoltage,
            target = target
    )

    @Deprecated(message = "Does not work in practice")
    override operator fun invoke(applying: V, drawing: I, target: V) = limit(
            emf = applying - drawing * motorR,
            target = target
    )

    private fun limit(emf: V, target: V): V {
        val expectedCurrent = (target - emf) / motorR

        return when {
            expectedCurrent > limit -> limit * motorR + emf
            expectedCurrent < -limit -> -(limit * motorR + emf)
            else -> target
        }
    }
}