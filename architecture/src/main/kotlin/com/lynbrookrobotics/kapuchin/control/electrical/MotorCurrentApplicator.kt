package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.*

class MotorCurrentApplicator(
        val maxVoltage: Volt, stall: Ampere,
        val freeSpeed: AngularVelocity
) : (AngularVelocity, Ampere) -> Volt {

    private val motorR: Ohm = maxVoltage / stall

    override operator fun invoke(speed: AngularVelocity, target: Ampere): Volt {
        val emf: Volt = speed / freeSpeed * maxVoltage
        return target * motorR + emf
    }
}