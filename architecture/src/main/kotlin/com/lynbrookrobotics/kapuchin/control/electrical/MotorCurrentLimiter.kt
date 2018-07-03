package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.AngularVelocity
import info.kunalsheth.units.generated.Ohm
import info.kunalsheth.units.generated.Volt

class MotorCurrentLimiter(
        val maxVoltage: Volt, val freeSpeed: AngularVelocity,
        stall: Ampere, val limit: Ampere
) : (AngularVelocity, Volt) -> Volt, (Volt, Ampere, Volt) -> Volt {
    private val motorR: Ohm = maxVoltage / stall

    override operator fun invoke(speed: AngularVelocity, target: Volt) = limit(
            emf = speed / freeSpeed * maxVoltage,
            target = target
    )

    override operator fun invoke(applying: Volt, drawing: Ampere, target: Volt) = limit(
            emf = applying - drawing * motorR,
            target = target
    )

    private fun limit(emf: Volt, target: Volt): Volt {
        val expectedCurrent = (target - emf) / motorR

        return when {
            expectedCurrent > limit -> limit * motorR + emf
            expectedCurrent < -limit -> -(limit * motorR + emf)
            else -> target
        }
    }
}