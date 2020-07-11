package com.lynbrookrobotics.kapuchin.tests.control.math.kinematics

import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.test.Test

class TrapezoidalMotionProfilingTest {

    val target = 20.Foot
    val speed = 10.FootPerSecond

    private val trap = trapezoidalMotionProfile(
            deceleration = 5.FootPerSecondSquared,
            topSpeed = speed
    )

    @Test
    fun `the robot should go backwards if it overshoots its stopping point`() {
        0.FootPerSecond `is equal to?` trap(/*error = */0.Foot)
        repeat(20) {
            val current = target + 1.Inch + it.Foot
            val profile = trap(target - current)

            profile `is within?` 0.FootPerSecond..-speed
        }
    }

    @Test
    fun `the robot should go forwards if it undershoots its stopping point`() {
        repeat(target.Foot.toInt() - 1) {
            val current = it.Foot + 1.Inch
            val profile = trap(target - current)

            profile `is within?` 0.FootPerSecond..speed
        }
    }

    @Test
    fun `the robot should go forwards if it overshoots its stopping point in reverse`() {
        val target = -target

        0.FootPerSecond `is equal to?` trap(/*error = */0.Foot)
        repeat(20) {
            val current = target - 1.Inch - it.Foot
            val profile = trap(target - current)

            profile `is within?` 0.FootPerSecond..speed
        }
    }

    @Test
    fun `the robot should go backwards if it undershoots its stopping point in reverse`() {
        val target = -target

        repeat(target.Foot.toInt() + 1) {
            val current = it.Foot - 1.Inch
            val profile = trap(target - current)

            profile `is within?` 0.FootPerSecond..-speed
        }
    }
}