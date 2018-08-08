package com.lynbrookrobotics.kapuchin.control.math.kinematics

import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import info.kunalsheth.units.generated.*

class TotalCurrentCalculator(
        val mass: Mass,
        val motorStallCurrent: Ampere,
        val motorStallTorque: Torque,
        val wheelRadius: Length,
        vararg val gearbox: GearTrain
) : (Acceleration) -> Ampere {

    override fun invoke(target: Acceleration): Ampere {
        val force: Force = mass * target
        val wheelTorque = force * wheelRadius

        val motorTorque = gearbox.fold(wheelTorque) { acc, gearTrain ->
            gearTrain.outputToInput(acc)
        }

        return 0.Ampere
    }
}