package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import java.io.File

private fun loadPath(name: String): Path =
        Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("com/lynbrookrobotics/kapuchin/paths/$name")
                .bufferedReader()
                .lineSequence()
                .drop(1)
                .map { it.split('\t') }
                .map { it.map { tkn -> tkn.trim() } }
                .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                .toList()

private fun loadTempPath(): Path =
        File("/home/lvuser/journal.tsv")
                .bufferedReader()
                .lineSequence()
                .drop(1)
                .map { it.split('\t') }
                .map { it.map { tkn -> tkn.trim() } }
                .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
                .toList()


suspend fun Subsystems.followJournal() = startChoreo("Follow journal") {
    val path = loadTempPath()

    val trajectory = pathToTrajectory(path, drivetrain.maxSpeed, drivetrain.maxOmega, drivetrain.maxAcceleration)

    System.gc()

    choreography {
        drivetrain.followTrajectory(trajectory, 12.Inch, 2.Inch, true)
        freeze()
    }
}