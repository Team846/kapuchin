package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

private val t = 1.Turn
private val ht = 0.5.Turn

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
    val difference = this % t - that % t

    return when {
        difference > ht -> difference - t
        difference < -ht -> difference + t
        else -> difference
    }
}

val UomVector<Length>.absSq get() = x * x + y * y
fun distanceSq(a: UomVector<Length>, b: UomVector<Length>) = (a - b).absSq

val UomVector<Length>.abs get() = Length(sqrt(absSq.siValue))
fun distance(a: UomVector<Length>, b: UomVector<Length>) = (a - b).abs

inline infix fun <Q : Quan<Q>> UomVector<Q>.midpt(that: UomVector<Q>) = UomVector(
        avg(this.x, that.x),
        avg(this.y, that.y),
        avg(this.z, that.z)
)