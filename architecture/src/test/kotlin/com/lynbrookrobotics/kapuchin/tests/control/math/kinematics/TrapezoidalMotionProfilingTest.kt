package com.lynbrookrobotics.kapuchin.tests.control.math.kinematics

import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.math.absoluteValue
import kotlin.test.Test

class TrapezoidalMotionProfilingTest {

    val dist = 20.Foot
    val speed = 10.FootPerSecond

    private val basicProfile = trapezoidalMotionProfile(
            distance = dist,
            startingSpeed = 0.FootPerSecond,
            acceleration = 5.FootPerSecondSquared,
            topSpeed = speed
    )

    private val backwardsProfile = trapezoidalMotionProfile(
            distance = -dist,
            startingSpeed = 0.FootPerSecond,
            acceleration = 5.FootPerSecondSquared,
            topSpeed = speed
    )

    @Test
    fun `the robot should go backwards if it overshoots its stopping point`() {
        val target = dist

        0.FootPerSecond `is equal to?` basicProfile(target)
        repeat(20) {
            val profileInput = target + 1.Inch + it.Foot
            val profile = basicProfile(profileInput)

            0.FootPerSecond `is greater than?` profile
            profile `is greater than?` basicProfile(profileInput + 1.Foot)
        }
    }

    @Test
    fun `the robot should go forwards if it undershoots its stopping point`() {
        val target = dist
        repeat(target.Foot.toInt() - 1) {
            val profileInput = it.Foot + 1.Inch
            val profile = basicProfile(profileInput)

            profile `is greater than?` 0.FootPerSecond
            speed `is greater than or equal to?` profile
        }
    }

    @Test
    fun `the robot should go forwards if it overshoots its stopping point in reverse`() {
        val target = -dist

        0.FootPerSecond `is equal to?` backwardsProfile(target)
        repeat(20) {
            val profileInput = target - 1.Inch - it.Foot
            val profile = backwardsProfile(profileInput)

            profile `is greater than?` 0.FootPerSecond
            backwardsProfile(profileInput - 1.Foot) `is greater than?` profile
        }
    }

    @Test
    fun `the robot should go backwards if it undershoots its stopping point in reverse`() {
        val target = -dist
        repeat(target.Foot.toInt().absoluteValue - 1) {
            val profileInput = -it.Foot - 1.Inch
            val profile = backwardsProfile(profileInput)

            0.FootPerSecond `is greater than?` profile
            profile `is greater than or equal to?` -speed
        }
    }
}