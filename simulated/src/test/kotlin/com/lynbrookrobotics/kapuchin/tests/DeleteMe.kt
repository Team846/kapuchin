package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import info.kunalsheth.units.generated.*
import org.junit.Test

class DeleteMe {
    @Test
    fun `awlefeawefefaw`() {
        val origin = Position(5.Foot, 0.Foot, 45.Degree)

        val path = nSect(Waypt(0.Foot, 0.Foot), Waypt(0.Foot, 8.Foot), 3.Inch)

        val trajectory = pathToTrajectory(path, 10.FootPerSecond, 1.Radian / Second, 3.FootPerSecondSquared)


        trajectory.forEach {
            println("${it.waypt.x.Foot}\t" +
                    "${it.waypt.y.Foot}\t")
//                    "${it.velocity.FootPerSecond}\t" +
//                    "${it.omega / Degree * Second}")
        }

        with(RotationMatrix(origin.bearing)) {
            trajectory.also { t -> t.forEach { it.waypt = rotate(it.waypt) + origin.vector } }
        }

        trajectory.forEach {
            println("${it.waypt.x.Foot}\t" +
                    "${it.waypt.y.Foot}\t")
//                    "${it.velocity.FootPerSecond}\t" +
//                    "${it.omega / Degree * Second}")
        }
    }
}