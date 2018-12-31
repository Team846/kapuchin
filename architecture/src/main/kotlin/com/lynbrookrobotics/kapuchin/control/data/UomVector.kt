package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.math.avg

/**
 * Represents a 3D vector
 *
 * @author Kunal
 *
 * @param T type of vector coordinates
 *
 * @property x x-coordinate
 * @property y y-coordinate
 * @property z z-coordinate
 */
interface Vector<T> {
    val x: T
    val y: T
    val z: T
}

data class UomVector<Q : Quan<Q>>(override val x: Q, override val y: Q, override val z: Q) : Vector<Q> {
    operator fun plus(other: UomVector<Q>) = UomVector(
            x + other.x,
            y + other.y,
            z + other.z
    )

    operator fun minus(other: UomVector<Q>) = UomVector(
            x - other.x,
            y - other.y,
            z - other.z
    )

    fun <QQ : Quan<QQ>> dot(times: (Q, Q) -> QQ, other: UomVector<Q>) =
            times(x, other.x) + times(y, other.y) + times(z, other.z)

    operator fun times(scalar: Number) = UomVector(
            x * scalar,
            y * scalar,
            z * scalar
    )
}

data class NumVector(override val x: Double, override val y: Double, override val z: Double) : Vector<Double>

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