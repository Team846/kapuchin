package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*
import java.io.BufferedReader
import java.io.File

/**
 * Load a path bundled in twenty/resources/paths.
 *
 * @param name of the path (without .tsv)
 */
fun loadResourcePath(name: String) = Thread.currentThread()
    .contextClassLoader
    .getResourceAsStream("com/lynbrookrobotics/twenty/paths/$name.tsv")
    ?.bufferedReader()
    ?.toPath()

/**
 * Load a trajectory bundled in twenty/resources/paths.
 *
 * @param name of the trajectory (without .tsv)
 */
fun loadResourceTrajectory(name: String) = Thread.currentThread()
    .contextClassLoader
    .getResourceAsStream("com/lynbrookrobotics/twenty/paths/$name.tsv")
    ?.bufferedReader()
    ?.toTrajectory()

/**
 * Load the most recently recorded path on robot.
 */
fun loadTempPath(id: Int = 0) = File("/home/lvuser/$id.tsv")
    .takeIf { it.exists() }
    ?.bufferedReader()
    ?.toPath()

/**
 * Load a path on robot home path.
 *
 * @param name of the path (without .tsv)
 */
fun loadRobotPath(name: String) = File("/home/lvuser/$name.tsv")
    .takeIf { it.exists() }
    ?.bufferedReader()
    ?.toPath()

/**
 * Load a trajectory on robot home path.
 *
 * @param name of the trajectory (without .tsv)
 */
fun loadRobotTrajectory(name: String) = File("/home/lvuser/$name.tsv")
    .takeIf { it.exists() }
    ?.bufferedReader()
    ?.toTrajectory()

private fun BufferedReader.toPath() = lineSequence()
    .drop(1)
    .map { it.split('\t') }
    .map { it.map { tkn -> tkn.trim() } }
    .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) }
    .toList()

private fun BufferedReader.toTrajectory() = lineSequence()
    .drop(1)
    .map { it.split('\t') }
    .map { it.map { tkn -> tkn.trim() } }
    .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) stampWith it[2].toDouble().Second }
    .toList()

fun Subsystems.fastAsFuckLine(dist: Length, speedFactor: DutyCycle = 100.Percent): Trajectory =
    pathToTrajectory(
        nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
        drivetrain.maxSpeed * speedFactor,
        drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor,
        drivetrain.maxAcceleration
    )

fun Subsystems.fastAsFuckPath(path: Path, speedFactor: Dimensionless = 100.Percent): Trajectory =
    pathToTrajectory(
        path,
        drivetrain.maxSpeed * speedFactor,
        drivetrain.percentMaxOmega * drivetrain.maxOmega * speedFactor,
        drivetrain.maxAcceleration
    )