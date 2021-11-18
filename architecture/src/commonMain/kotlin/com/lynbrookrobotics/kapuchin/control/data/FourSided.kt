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
data class FourSided<out T>(val tr: T, val tl: T, val bl: T, val br: T) {
    constructor(bothSides: T) : this(bothSides, bothSides, bothSides, bothSides)
}

val <Q : Quan<Q>> FourSided<Q>.avg get() = avg(tr, tl, br, bl)

operator fun <Q : Quan<Q>> FourSided<Q>.plus(that: FourSided<Q>) = FourSided(
    this.tr + that.tr,
    this.tl + that.tl,
    this.bl + that.bl,
    this.br + that.br,
)

operator fun <Q : Quan<Q>> FourSided<Q>.minus(that: FourSided<Q>) = FourSided(
    this.tr - that.tr,
    this.tl - that.tl,
    this.bl - that.bl,
    this.br - that.br,
)