package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Represents data that has a left and right side
 *
 * Intended for representing two-sided systems like a tank drive or a two-belt shooter
 *
 * @author Kunal
 *
 * @param T type of each side
 * @property left value of the left side
 * @property right value of the right side
 */
data class TwoSided<out T>(val left: T, val right: T) {
    constructor(bothSides: T) : this(bothSides, bothSides)
}

val <Q : Quan<Q>> TwoSided<Q>.avg get() = avg(left, right)

operator fun <Q : Quan<Q>> TwoSided<Q>.plus(that: TwoSided<Q>) = TwoSided(
    this.left + that.left,
    this.right + that.right
)

operator fun <Q : Quan<Q>> TwoSided<Q>.minus(that: TwoSided<Q>) = TwoSided(
    this.left - that.left,
    this.right - that.right
)