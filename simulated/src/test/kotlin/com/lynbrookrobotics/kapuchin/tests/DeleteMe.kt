package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import org.junit.Test

class DeleteMe {


    @Test
    fun `awlefeafdjwfffaeffefaw`() {
        val vMax = 1.Foot / Second
        val omegaMax = 1.Radian / Second
        val aMax = 0.5.Foot / Second / Second

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

        val trajectory = pathToTrajectory(path2, vMax, omegaMax, aMax)

        trajectory.forEach {
            println("${it.x.Second}\t${it.y.x.Foot}\t${it.y.y.Foot}")
        }
    }
}