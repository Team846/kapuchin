package com.lynbrookrobotics.kapuchin.control.math.trajectory

import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import kotlin.math.ceil

fun toPath(waypts: Path, nSect: Length): Path {
    val start = waypts.first()
    val end = waypts.last()

    val split = waypts
            .zipWithNext { t, n -> nSect(t, n, nSect) }
            .map { it - waypts }
            .flatten()

    return listOf(start) + split + end
}

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