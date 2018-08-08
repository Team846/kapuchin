package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.control.div

class MotorCurrentApplicator(
        val maxVoltage: Volt, val freeSpeed: AngularVelocity, stall: Ampere
) : (AngularVelocity, Ampere) -> Volt {

    private val motorR: Ohm = maxVoltage / stall

    override operator fun invoke(speed: AngularVelocity, target: Ampere): Volt {
        val emf: Volt = speed / freeSpeed * maxVoltage
        return target * motorR + emf
    }
}