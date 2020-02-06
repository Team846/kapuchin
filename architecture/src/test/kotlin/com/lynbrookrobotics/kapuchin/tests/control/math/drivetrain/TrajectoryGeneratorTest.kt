package com.lynbrookrobotics.kapuchin.tests.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test

class TrajectoryGeneratorTest {

    private val maxVelocity = 10.Foot / Second
    private val maxOmega = 4.Radian / Second
    private val maxAcceleration = 4.Foot / Second / Second

    // https://www.desmos.com/calculator/ihzjjn1gzh
    private val path = (0 until 1000)
            .map { it / 500.0 }
            .map { Waypoint(sin(5 * PI * it).Foot, (cos(7 * PI * it) - 1).Foot) }

    @Test
    fun `trajectories never exceed max velocity`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)

        for (i in 1 until trajectory.size - 2) {
            val dx = distance(trajectory[i].y, trajectory[i - 1].y)
            val dt = trajectory[i].x - trajectory[i - 1].x
            val v = dx / dt

            102.Percent * maxVelocity `is greater than?` v
        }
    }

    @Test
    fun `trajectories never exceed max acceleration`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)

        var oldV = 0.Foot / Second
        for (i in 1 until trajectory.size - 2) {
            val dx = distance(trajectory[i].y, trajectory[i - 1].y)
            val dt = trajectory[i].x - trajectory[i - 1].x
            val v = dx / dt

            // v² = v₀² + 2aΔx
            val a = (v * v - oldV * oldV) / 2 / dx
            102.Percent * maxAcceleration `is greater than?` a

            oldV = v
        }
    }
}