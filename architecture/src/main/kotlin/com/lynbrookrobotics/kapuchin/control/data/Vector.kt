package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*

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

data class UomVector<Q : Quan<Q>>(
        override val x: Q,
        override val y: Q,
        override val z: Q = x * 0
) : Vector<Q> {

    operator fun plus(that: UomVector<Q>) = UomVector(
            x + that.x,
            y + that.y,
            z + that.z
    )

    operator fun minus(that: UomVector<Q>) = UomVector(
            this.x - that.x,
            this.y - that.y,
            this.z - that.z
    )

    operator fun times(that: Double) = UomVector(
            this.x * that,
            this.y * that,
            this.z * that
    )

    operator fun div(that: Double) = UomVector(
            this.x / that,
            this.y / that,
            this.z / that
    )

}

data class NumVector(
        override val x: Double,
        override val y: Double,
        override val z: Double
) : Vector<Double>