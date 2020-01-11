package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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

    fun rotateX(x: Length, y: Length) = x * cos + y * sin

    fun rotateY(x: Length, y: Length) = -x * sin + cos * y

    fun rotate(that: UomVector<Length>) = that.run {
        UomVector(
                rotateX(x, y),
                rotateY(x, y),
                z
        )
    }
}