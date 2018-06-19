package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.*

class MotorCurrentLimiter(
        val maxVoltage: Volt, val freeSpeed: AngularVelocity,
        stall: Ampere, val limit: Ampere
) : (AngularVelocity, Volt) -> Volt {
    private val motorR: Ohm = maxVoltage / stall

    override operator fun invoke(currentSpeed: AngularVelocity, target: Volt): Volt {
        val emf = emf(currentSpeed, freeSpeed, maxVoltage)
        val expectedCurrent = (target - emf) / motorR

        return when {
            expectedCurrent > limit -> limit * motorR + emf
            expectedCurrent < -limit -> -(limit * motorR + emf)
            else -> target
        }
    }

    private fun emf(currentSpeed: AngularVelocity, freeSpeed: AngularVelocity, maxVoltage: Volt): Volt =
            (currentSpeed / freeSpeed) * maxVoltage
}