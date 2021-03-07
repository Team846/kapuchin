package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import kotlin.math.E
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test

class TrajectoryFollowerTest {

    @Test
    fun `isBehind correctly returns true at 0 deg bearing`() {
        val point = Waypoint(0.Foot, 0.Foot)
        val pos = Position(0.Foot, 1.Foot, 0.Degree)

        point isBehind pos `is equal to?` true
    }

    @Test
    fun `isBehind correctly returns false at 0 deg bearing`() {
        val point = Waypoint(0.Foot, 0.Foot)
        val pos = Position(0.Foot, -1.Foot, 0.Degree)

        point isBehind pos `is equal to?` false
    }

    @Test
    fun `isBehind correctly returns true at non-zero bearing`() {
        // 30 60 90 triangle
        val point = Waypoint(0.Foot, (sqrt(3.0)).Foot)
        val pos = Position(1.Foot, 0.Foot, -29.Degree + 90.Degree)

        point isBehind pos `is equal to?` true
    }

    @Test
    fun `isBehind correctly returns false at non-zero bearing`() {
        // 30 60 90 triangle
        val point = Waypoint(0.Foot, (sqrt(3.0)).Foot)
        val pos = Position(1.Foot, 0.Foot, -31.Degree + 90.Degree)

        point isBehind pos `is equal to?` false
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
    fun blah() {
        val maxExtrapolate = 50.Inch
        val maxSpeed = 15.Foot / Second
        fun extrapDist(speed: Velocity) =
            maxExtrapolate / (1 + E.pow((-(speed - maxSpeed / 2) * Second / Metre).Each))

        assert(extrapDist(7.5.Foot / Second) == maxExtrapolate / 2)
    }
}