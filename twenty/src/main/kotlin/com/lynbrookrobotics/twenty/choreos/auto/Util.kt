package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.followTrajectory
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

fun Subsystems.fastAsFuckLine(dist: Length, config: AutoPathConfiguration): Trajectory =
    pathToTrajectory(
        nSect(Waypoint(0.Foot, 0.Foot), Waypoint(0.Foot, dist), 3.Inch),
        drivetrain.maxSpeed * config.speedFactor,
        drivetrain.maxOmega * config.percentMaxOmega * config.speedFactor,
        config.maxAccel,
        config.maxDecel
    )

fun Subsystems.fastAsFuckPath(path: Path, config: AutoPathConfiguration): Trajectory =
    pathToTrajectory(
        path,
        drivetrain.maxSpeed * config.speedFactor,
        drivetrain.maxOmega * config.percentMaxOmega * config.speedFactor,
        config.maxAccel,
        config.maxDecel
    )

suspend fun Subsystems.timePath(config: AutoPathConfiguration) = loadRobotPath(config.name)?.let { path ->
    val trajectory = fastAsFuckPath(path, config)
//    val time = measureTimeMillis {
    drivetrain.followTrajectory(trajectory, config)
//    }.milli(Second)

//    log(Debug) { "${config.name} finished: ${time.Second withDecimals 3}s" }
} ?: log(Error) { "Couldn't find path ${config.name}" }