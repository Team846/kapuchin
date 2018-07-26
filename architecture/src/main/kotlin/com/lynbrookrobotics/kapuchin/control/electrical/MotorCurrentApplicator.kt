package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.AngularVelocity
import info.kunalsheth.units.generated.Ohm
import info.kunalsheth.units.generated.Volt

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