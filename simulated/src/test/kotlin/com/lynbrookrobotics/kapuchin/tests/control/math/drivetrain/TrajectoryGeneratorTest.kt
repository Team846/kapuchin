package com.lynbrookrobotics.kapuchin.tests.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test

class TrajectoryGeneratorTest {

    private fun waypt(t: Double) = Waypt(
            sin(5 * PI * t).Metre,
            cos(7 * PI * t).Metre
    )

    private val maxVelocity = 10.Metre / Second
    private val maxOmega = 4.Radian / Second
    private val maxAcceleration = 4.Metre / Second / Second
    private val maxAlpha = 2.Radian / Second / Second

    private val path = (0 until 2000)
            .map { it / 1000.0 }
            .map { waypt(it) }

    @Test
    fun `trajectories never exceed max constants`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration, maxAlpha)
        trajectory.forEach {
            maxVelocity `is greater than or equal to?` it.velocity
            maxAcceleration `is greater than or equal to?` it.acceleration

            it.velocity `is greater than or equal to?` -maxVelocity
            it.acceleration `is greater than or equal to?` -maxAcceleration
        }
    }

    @Test
    fun `trajectories start and end at zero velocity`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration, maxAlpha)
        trajectory.first().velocity `is equal to?` 0.Metre / Second
        trajectory.last().velocity `is equal to?` 0.Metre / Second
    }

    @Test
    fun `trajectories have valid timestamps and velocities`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration, maxAlpha)
        for (i in 1 until trajectory.size) {
            val s1 = trajectory[i - 1]
            val s2 = trajectory[i]

            val dx = distance(s1.waypt, s2.waypt)
            val dt = s2.time - s1.time

            dt * (s1.velocity + (s2.velocity - s1.velocity) / 2) `is equal to?` dx
        }
    }

    @Test
    fun `trajectories have valid accelerations`() {
        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration, maxAlpha)
        for (i in 1 until trajectory.size) {
            val s1 = trajectory[i - 1]
            val s2 = trajectory[i]

            val dv = s2.velocity - s1.velocity
            val dt = s2.time - s1.time

            dv / dt `is equal to?` s2.acceleration
        }
    }

}