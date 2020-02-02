//package com.lynbrookrobotics.kapuchin.tests.control.math.drivetrain
//
//import com.lynbrookrobotics.kapuchin.control.data.*
//import com.lynbrookrobotics.kapuchin.control.math.*
//import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
//import com.lynbrookrobotics.kapuchin.tests.*
//import info.kunalsheth.units.generated.*
//import kotlin.math.PI
//import kotlin.math.cos
//import kotlin.math.sin
//import kotlin.test.Test
//
//class TrajectoryGeneratorTest {
//
//    private val maxVelocity = 10.Foot / Second
//    private val maxOmega = 4.Radian / Second
//    private val maxAcceleration = 4.Foot / Second / Second
//
//    // https://www.desmos.com/calculator/ihzjjn1gzh
//    private val path = (0 until 2000)
//            .map { it / 1000.0 }
//            .map { Waypt(sin(5 * PI * it).Foot, cos(7 * PI * it).Foot) }
//
//    @Test
//    fun `trajectory segments never exceed max velocity`() {
//        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)
//        trajectory.forEach {
//            maxVelocity `is greater than?` it.velocity
//        }
//    }
//
//    @Test
//    fun `trajectory segments never exceed max acceleration`() {
//        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)
//        trajectory.zipWithNext { s1, s2 ->
//            // v² = v₀² + 2aΔx
//            val dx = distance(s1.waypt, s2.waypt)
//            val acceleration = (s2.velocity * s2.velocity - s1.velocity * s1.velocity) / 2 / dx
//
//            // 101% to compensate for floating point error
//            acceleration `is within?` (-maxAcceleration * 101.Percent)..(maxAcceleration * 101.Percent)
//        }
//    }
//
//    @Test
//    fun `trajectories start and end at zero velocity`() {
//        val trajectory = pathToTrajectory(path, maxVelocity, maxOmega, maxAcceleration)
//        trajectory.first().velocity `is equal to?` 0.Foot / Second
//        trajectory.last().velocity `is equal to?` 0.Foot / Second
//    }
//
//}