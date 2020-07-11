package com.lynbrookrobotics.kapuchin.control.data

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Represents a rotation matrix
 * Uses compass bearing, not trigonometric angle!
 *
 * @author Kunal
 *
 * @property theta rotation about the origin
 */
data class RotationMatrix(val theta: Angle) {
    internal val sin = sin(theta)
    internal val cos = cos(theta)

    /**
     * Rotate a vector about the Z-axis
     * Intended for high-performance applications like high-frequency odometry
     *
     * @param x x coordinate of vector to rotate
     * @param y y coordinate of vector to rotate
     * @return y coordinate of rotated vector
     */
    fun rzCoordinateY(x: Length, y: Length) = -x * sin + y * cos

    /**
     * Rotate a vector about the Z-axis
     * Intended for high-performance applications like high-frequency odometry
     *
     * @param x x coordinate of vector to rotate
     * @param y y coordinate of vector to rotate
     * @return x coordinate of rotated vector
     */
    fun rzCoordinateX(x: Length, y: Length) = x * cos + y * sin

    /**
     * Rotate a vector about the Z-axis
     *
     * @param that vector to rotate
     * @return rotated vector
     */
    infix fun rz(that: UomVector<Length>) = that.run {
        UomVector(
            rzCoordinateX(x, y),
            rzCoordinateY(x, y),
            z
        )
    }
}