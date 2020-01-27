package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import info.kunalsheth.units.generated.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DeleteMe {
    @Test
    fun `awlefeajwfefefaw`() {
        val path = nSect(Waypt(0.Foot, 0.Foot), Waypt(0.Foot, 8.Foot), 3.Inch)

        val trajectory = pathToTrajectory(path, 10.FootPerSecond, 1.Radian / Second, 3.FootPerSecondSquared)

        trajectory.forEach {
            println("${it.waypt.x.Foot}\t" +
                    "${it.waypt.y.Foot}\t" +
                    "${it.velocity.FootPerSecond}\t" +
                    "${it.omega / Degree * Second}")
        }
    }
}