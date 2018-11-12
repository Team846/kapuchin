package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.*

class MotorCurrentLimiter(
        val maxVoltage: V, val freeSpeed: AngularVelocity,
        stall: I, val limit: I
) :
        (AngularVelocity, V) -> V,
        (V, I, V) -> V {

    private val motorR: ElectricalResistance = maxVoltage / stall

    override operator fun invoke(speed: AngularVelocity, target: V) = limit(
            emf = speed / freeSpeed * maxVoltage,
            target = target
    )

    // todo this is broken
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