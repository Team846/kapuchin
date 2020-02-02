package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import info.kunalsheth.units.generated.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DeleteMe {

    @Test
    fun `awefeafdjwfffaeffefaw`() {
        val vMax = 10.Foot / Second
        val omegaMax = 4.Radian / Second
        val aMax = 4.Foot / Second / Second

        val path = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("path.tsv")
                .bufferedReader()
                .lineSequence()
                .map { it.split('\t') }
                .map { it.map { tkn -> tkn.trim() } }
                .map { Waypt(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                .toList()

        val path2 = nSect(Waypt(0.Foot, 0.Foot), Waypt(0.Foot, 20.Foot), 3.Inch)

        val path3 = (0 until 1000)
                .map { it / 500.0 }
                .map { Waypt(sin(5 * PI * it).Foot, (cos(7 * PI * it) - 1).Foot) }

        val trajectory = pathToTrajectory(path2, vMax, omegaMax, aMax)

        trajectory.forEach {
            println("${it.x.Second}\t${it.y.x.Foot}\t${it.y.y.Foot}")
        }
    }
}