package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

data class Motor(
        val voltage: V,
        val freeSpeed: AngularVelocity,
        val stallCurrent: I,
        val stallTorque: Torque
) {
    val windings = voltage / stallCurrent
}