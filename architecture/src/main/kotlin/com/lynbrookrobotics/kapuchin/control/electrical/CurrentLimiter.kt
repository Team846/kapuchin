package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Volt

class CurrentLimiter(
    val limit: Ampere
) : (Ampere, Volt, Volt) -> Volt {
    override operator fun invoke(currDraw: Ampere, currApplied: Volt, target: Volt): Volt {
        val r = currApplied / currDraw
        val expectedCurrent = target / r

        return when {
            expectedCurrent > limit -> limit * r
            expectedCurrent < -limit -> -limit * r
            else -> target
        }
    }
}