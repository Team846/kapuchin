package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Ignore
import kotlin.test.Test

class PathingTest {

    @Test
    @Ignore // takes too long
    fun `nSect returns equal segments`() {
        anyDouble.forEach { x1 ->
            anyDouble.forEach { y1 ->
                anyDouble.forEach { x2 ->
                    anyDouble
                            .filterNot { y2 -> x1 == x2 && y1 == y2 }
                            .forEach { y2 ->

                                anyDouble.map { it.Metre }.forEach { len ->
                                    val a = Waypt(x1.Metre, y1.Metre)
                                    val b = Waypt(x2.Metre, y2.Metre)

                                    val pts = nSect(a, b, len)

                                    pts.size `is greater than or equal to?` 2

                                    if (pts.size > 2) pts
                                            .zipWithNext { p1, p2 -> distance(p1, p2) }
                                            .forEach { len `is greater than or equal to?` it }

                                    pts.zipWithNext().forEach { (p1, p2) ->
                                        val da1 = distance(a, p1)
                                        val da2 = distance(a, p2)

                                        val db1 = distance(b, p1)
                                        val db2 = distance(b, p2)

                                        da2 `is greater than?` da1
                                        db1 `is greater than?` db2
                                    }
                                }
                            }
                }
            }
        }
    }

    @Test
    fun `toPath smooths waypoints`() {
        val waypts = listOf(
                Waypt(-1.Metre, -1.Metre),
                Waypt(-1.Metre, 0.Metre),
                Waypt(1.Metre, 0.Metre),
                Waypt(1.Metre, 1.Metre)
        )

        val len = 0.1.Metre
        val path = toPath(waypts, len)
        val segs = path.zipWithNext()

        segs
                .map { (a, b) -> distance(a, b) }
                .forEach { len * 2 `is greater than or equal to?` it }


        val start = waypts.first()
        val end = waypts.last()

        path.zipWithNext().forEach { (p1, p2) ->
            val da1 = distance(start, p1)
            val da2 = distance(start, p2)

            val db1 = distance(end, p1)
            val db2 = distance(end, p2)

            da2 `is greater than?` da1
            db1 `is greater than?` db2
        }

        (waypts - start - end).none { it in path } `is equal to?` true
    }

    @Test
    fun `pathToTrajectory produces viable trajectories`() {
        fun path(t: Double) = Waypt(
                sin(5 * PI * t).Metre,
                cos(7 * PI * t).Metre
        )

        val waypts = (0 until 2000)
                .map { it / 1000.0 }
                .map { path(it) }

        val maxV = 1.Metre / Second
        val maxOmega = 1.Radian / Second
        val performance = 75.Percent

        val traj = pathToTrajectory(waypts,
                performance,
                maxV, maxOmega
        )

        traj
                .zipWithNext()
                .zipWithNext { (p1, p2), (_, p3) ->
                    val vel1 = distance(p2.y, p1.y) / (p2.x - p1.x)
                    val vel2 = distance(p3.y, p2.y) / (p3.x - p2.x)

                    maxV `is greater than or equal to?` vel1
                    maxV `is greater than or equal to?` vel2

                    val deltaTheta = (atan2(
                            p2.y.x - p1.y.x, p2.y.y - p1.y.y
                    ) - atan2(
                            p3.y.x - p2.y.x, p3.y.y - p2.y.y
                    )) `coterminal -` 0.Degree

                    val omega2 = deltaTheta / (p3.x - p2.x)

                    maxOmega `is greater than or equal to?` omega2

                    abs(vel2 / maxV) + abs(omega2 / maxOmega)
                }
                .dropLast(1)
                .forEach {
                    it `is within?` (performance `±` 15.Percent)
                }
    }
}