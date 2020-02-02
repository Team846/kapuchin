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
            .map { Waypt(sin(5 * PI * it).Foot, (cos(7 * PI * it) - 1).Foot) }

    @Test
    fun `trajectories never exceed max velocity`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)

        for (i in 1 until trajectory.size - 1) {
            val dx = distance(trajectory[i].y, trajectory[i - 1].y)
            val dt = trajectory[i].x - trajectory[i - 1].x
            val v = dx / dt

            102.Percent * maxVelocity `is greater than?` v
        }
    }

    @Test
    fun `trajectories never exceed max omega`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)

        for (i in 2 until trajectory.size - 1) {
            val p1 = trajectory[i - 2].y
            val p2 = trajectory[i - 1].y
            val p3 = trajectory[i].y // current

            val dtheta = -((p2 - p1).bearing `coterminal -` (p3 - p2).bearing)
            val dt = trajectory[i].x - trajectory[i - 1].x
            val omega = dtheta / dt

            102.Percent * maxOmega `is greater than?` omega.abs
        }
    }

    @Test
    fun `trajectories never exceed max acceleration`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)

        var oldV = 0.Foot / Second
        for (i in 1 until trajectory.size - 1) {
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