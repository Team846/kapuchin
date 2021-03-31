package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import kotlin.math.sqrt
import kotlin.test.Test

class TrajectoryFollowerTest {

    @Test
    fun `isBehind correctly returns true when robot is behind line at 0 bearing`() {
        val point = Waypoint(0.Foot, 0.Foot)
        val prev = Waypoint(0.Foot, -1.Foot)
        val pos = Position(0.Foot, -1.Foot, 0.Degree)
        val known = Waypoint(0.Foot, 1.Foot)

        pos.isBehind(point, prev, known) `is equal to?` true
    }

    @Test
    fun `isBehind correctly returns false when robot is past point at 0 deg bearing`() {
        val point = Waypoint(0.Foot, 0.Foot)
        val prev = Waypoint(0.Foot, -1.Foot)
        val pos = Position(0.Foot, 1.Foot, 0.Degree)
        val known = Waypoint(0.Foot, 2.Foot)

        pos.isBehind(point, prev, known) `is equal to?` false
    }

    @Test
    fun `isBehind correctly returns true at non-zero bearing`() {
        // 30 60 90 triangle
        val point = Waypoint(1.Foot, 0.Foot)
        val prev = Waypoint(0.Foot, 0.Foot)
        val pos = Position(1.Foot, 0.Foot, -29.Degree + 90.Degree)
        val known = Waypoint(2.Foot, 0.Foot)

        pos.isBehind(point, prev, known) `is equal to?` true
    }

    @Test
    fun `isBehind correctly returns false at non-zero bearing`() {
        val point = Waypoint(10.Foot, 10.Foot)
        val prev = Waypoint(8.Foot, 8.Foot)
        val pos = Position(11.Foot, 10.Foot, 47.Degree)
        val known = Waypoint(30.Foot, 30.Foot)

        pos.isBehind(point, prev, known) `is equal to?` false
    }

    @Test
    fun `extrapolate points are all collinear`() {
        val p1 = Waypoint(0.Foot, 0.Foot)
        val p0 = Waypoint(-3.Foot, -4.Foot)

        val p2 = (p1 stampWith 0.Second).extrapolate(p0, 5.Foot).y

        // Compare slope between p0 & p1 and p1 & p2 to see if collinear
        (p1.y - p0.y) * (p2.x - p1.x) `is equal to?` (p2.y - p1.y) * (p1.x - p0.x)
    }

    @Test
    fun `extrapolate distance is correct`() {
        val p1 = Waypoint(0.Foot, 0.Foot)
        val p0 = Waypoint(-3.Foot, -4.Foot)

        val p2 = (p1 stampWith 0.Second).extrapolate(p0, 5.Foot).y

        distance(p1, p2) `is equal to?` 5.Foot
    }

    @Test
    fun `extrapolate works correctly`() {
        val p1 = Waypoint(0.Foot, 0.Foot)
        val p0 = Waypoint(-3.Foot, -4.Foot)

        val p2 = (p1 stampWith 0.Second).extrapolate(p0, 5.Foot).y

        p2.x `is equal to?` 3.Foot
        p2.y `is equal to?` 4.Foot
    }

    @Test
    fun `extrapolation distance works`() {
        val maxExtrap = 50.Inch
        val extrapK = 1.0
        val maxSpeed = 10.Foot / Second

        listOf(0.Foot / Second, maxSpeed / 2, maxSpeed)
            .map { extrapolateDist(maxExtrap, extrapK, it, maxSpeed) }
            .map { it.Inch }
            .forEach(::println)

    }
}