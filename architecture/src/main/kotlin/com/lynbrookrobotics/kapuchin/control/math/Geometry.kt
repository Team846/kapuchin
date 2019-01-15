package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Turn

private val t = 1.Turn

/**
 * Calculates the difference between two angles
 *
 * Let C(θ) be the set of all co-terminal angles to θ.
 * Given θ₁ | θ₁ ϵ ℝ and θ₂ | θ₂ ϵ ℝ
 *
 * @author Sidhant, Kunal
 *
 * @return the smallest difference between an element of C(θ₁) and an element of C(θ₂)
 */
infix fun Angle.`coterminal -`(that: Angle): Angle {
    val difference = (this % t + t) % t - (that % t + t) % t
    return if (difference > 0.5.Turn) t - difference else difference
}