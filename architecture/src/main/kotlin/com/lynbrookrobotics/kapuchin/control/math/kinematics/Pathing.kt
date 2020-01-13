package com.lynbrookrobotics.kapuchin.control.math.kinematics

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.ceil

typealias Waypt = UomVector<Length>

fun toPath(
        waypts: List<Waypt>,
        nSect: Length
): List<Waypt> {
    val start = waypts.first()
    val end = waypts.last()

    val split = waypts
            .zipWithNext { t, n -> nSect(t, n, nSect) }
            .map { it - waypts }
            .flatten()

    return listOf(start) + split + end
}

fun nSect(a: Waypt, b: Waypt, cut: Length): List<Waypt> {
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

fun pathToTrajectory(waypts: List<Waypt>, performance: Dimensionless, maxV: Velocity, maxOmega: AngularVelocity): List<TimeStamped<Waypt>> {
    val numPts = waypts.size

    val first = abs(distance(waypts[0], waypts[1]) / performance / maxV)

    val times = mutableListOf(0.Second, first)

    var tSum = first
    for (i in 2 until numPts) {
        val p1 = waypts[i - 2]
        val p2 = waypts[i - 1]
        val p3 = waypts[i - 0]

        val a = distance(p1, p2)
        val `a²` = a * a

        val `ΔP` = distance(p2, p3)
        val `ΔP²` = `ΔP` * `ΔP`

        val c = distance(p1, p3)
        val `c²` = c * c

        val `Δθ` = 180.Degree - acos(
                (`a²` + `ΔP²` - `c²`) / (a * `ΔP` * 2)
        )

        tSum += abs(`Δθ` / performance / maxOmega) + abs(`ΔP` / performance / maxV)
        times += tSum
    }

    return waypts
            .zip(times)
            .map { (pt, t) -> pt stampWith t }
}