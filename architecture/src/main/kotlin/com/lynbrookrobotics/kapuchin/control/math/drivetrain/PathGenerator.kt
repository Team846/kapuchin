package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import kotlin.math.ceil

/**
 * Interpolates new waypoints between each pair of waypoints in a path.
 *
 * @param path a path with sparse waypoints.
 * @param cut the distance between each new waypoint.
 *
 * @return a new path with more waypoints.
 */
fun interpolatePath(path: Path, cut: Length): Path {
    val start = path.first()
    val end = path.last()

    val split = path
            .zipWithNext { t, n -> nSect(t, n, cut) }
            .map { it - path }
            .flatten()

    return listOf(start) + split + end
}

/**
 * Cuts the path between two points into multiple waypoints.
 *
 * @param a the starting waypoint.
 * @param b the ending waypoint.
 * @param cut the distance between each new waypoint.
 *
 * @return a path of wayoints in a straight line.
 */
fun nSect(a: Waypt, b: Waypt, cut: Length): Path {
    val dist = distance(a, b)
    val n = ceil((dist / cut).Each).toInt().takeIf { it > 1 } ?: 1

    return (n downTo 0)
            .map { it.toDouble() / n }
            .map { weight ->
                Waypt(
                        a.x * weight + b.x * (1 - weight),
                        a.y * weight + b.y * (1 - weight)
                )
            }
}