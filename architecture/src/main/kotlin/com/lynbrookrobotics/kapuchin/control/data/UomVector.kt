package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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

data class UomVector<Q : Quan<Q>>(override val x: Q, override val y: Q, override val z: Q = x * 0) : Vector<Q>

operator fun <Q : Quan<Q>> UomVector<Q>.plus(that: UomVector<Q>) = UomVector(
        this.x + that.x,
        this.y + that.y,
        this.z + that.z
)

operator fun <Q : Quan<Q>> UomVector<Q>.minus(that: UomVector<Q>) = UomVector(
        this.x - that.x,
        this.y - that.y,
        this.z - that.z
)

operator fun <Q : Quan<Q>> UomVector<Q>.times(that: Double) = UomVector(
        this.x * that,
        this.y * that,
        this.z * that
)

operator fun <Q : Quan<Q>> UomVector<Q>.div(that: Double) = UomVector(
        this.x / that,
        this.y / that,
        this.z / that
)

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

/**
 * Represents a robot location
 *
 * @author Kunal
 *
 * @property x robot cartesian coordinate
 * @property y robot cartesian coordinate
 * @property theta robot bearing
 */
// todo: unit test!
data class Position(val x: Length, val y: Length, val bearing: Angle)

operator fun Position.plus(that: Position) = Position(
        this.x + that.x,
        this.y + that.y,
        this.bearing + that.bearing
)

operator fun Position.minus(that: Position) = Position(
        this.x - that.x,
        this.y - that.y,
        this.bearing - that.bearing
)

val Position.vector get() = UomVector(x, y)

/**
 * Represents a rotation matrix
 *
 * @author Kunal
 *
 * @property theta rotation about the origin
 */
// todo: unit test!
data class RotationMatrix(val theta: Angle) {
    internal val sin = sin(theta)
    internal val cos = cos(theta)
}

infix fun RotationMatrix.rz(that: UomVector<Length>) = that.run {
    UomVector(
            x * cos + y * sin,
            -x * sin + cos * y,
            z
    )
}

fun RotationMatrix.rzComponentX(x: Length, y: Length) =
        x * cos + y * sin


fun RotationMatrix.rzComponentY(x: Length, y: Length) =
        -x * sin + cos * y


fun RotationMatrix.rzComponentZ(x: Length, y: Length, z: Length) =
        z
