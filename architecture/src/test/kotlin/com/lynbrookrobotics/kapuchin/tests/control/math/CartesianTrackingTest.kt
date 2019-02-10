package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.control.math.CartesianTrackingTest.Direction.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.absoluteValue
import kotlin.test.Test

class CartesianTrackingTest {
    /*
    In 2019, we call rotation matrix tracking every 0.03 mm
     */
    @Test
    fun `Rotation`() {
        anyDouble.filter { it.absoluteValue > 1 }.map { 0.03 + 1 / it }.forEach { constant ->
            val tracking = RotationMatrixTracking(2.Foot, Position(0.Foot, 0.Foot, 0.Degree))
            tracking(constant.milli(Metre), -constant.milli(Metre))
            val x = tracking.x
            val y = tracking.y
            x `is within?` (0.Foot `±` 0.1.Inch)
            y `is within?` (0.Foot `±` 0.1.Inch)
        }
    }

    @Test
    fun `Go forward 5 feet`() {
        var sum = 0.Foot
        val tracking = RotationMatrixTracking(2.Foot, Position(0.Foot, 0.Foot, 0.Degree))

        while (sum < 5.Foot) {
            anyDouble.filter { it > 1.0 }.forEach { constant ->
                val dist = 0.03.milli(Metre) + (0.1 / constant).milli(Metre)
                tracking(dist, dist)
                sum += dist
            }
        }

        tracking(5.Foot - sum, 5.Foot - sum)

        tracking.x `is within?` (0.Foot `±` 0.1.Inch)
        tracking.y `is within?` (5.Foot `±` 0.1.Inch)
        tracking.bearing `is equal to?` 0.Degree
    }

    @Test
    fun `Figure 8`() {
        var pos = Position(0.Foot, 0.Foot, 0.Degree)

        pos = arc(pos, 2.Foot, 180.Degree, Left)
        pos = arc(pos, 2.Foot, 180.Degree, Right)
        pos = arc(pos, 2.Foot, 180.Degree, Right)
        pos = arc(pos, 2.Foot, 180.Degree, Left)

        pos.x `is within?` (0.Foot `±` 0.1.Inch)
        pos.y `is within?` (0.Foot `±` 0.1.Inch)
        pos.bearing `is within?` (0.Degree `±` 1.Degree)
    }

//    @Test
//    fun `Spiral`() {
//        var pos = Position(0.Foot, 0.Foot, 0.Degree)
//
//        var sum = 0.Degree
//
//        val tracking = rotationMatrixTracking(2.Foot, pos)
//
//        while (sum < 360.Degree) {
//            anyDouble.filter { it > 1.0 }.forEach { constant ->
//                val dist = 0.03.milli(Metre) + (0.1 / constant).milli(Metre)
//
//            }
//        }
//
//
//        pos.x `is within?` (0.Foot `±` 0.1.Inch)
//        pos.y `is within?` (0.Foot `±` 0.1.Inch)
//        pos.bearing `is within?` (360.Degree `±` 1.Degree)
//    }

    enum class Direction {
        Left, Right
    }

    private fun arc(init: Position, radius: Length, angle: Angle, direction: Direction): Position {
        var sum = 0.Degree

        val tracking = RotationMatrixTracking(2.Foot, init)

        var r = radius

        while (sum < angle) {
            anyDouble.filter { it > 1.0 }.forEach { constant ->
                val dist = 0.03.milli(Metre) + (0.1 / constant).milli(Metre)
                val theta = dist / r * Radian

                val track = when (direction) {
                    Left -> 1.Foot
                    Right -> (-1).Foot
                }

                val distl = theta * (r - track) / Radian
                val distr = theta * (r + track) / Radian

                sum += theta

                tracking(distl, distr)
            }
        }

        return Position(tracking.x, tracking.y, tracking.bearing)
    }
}