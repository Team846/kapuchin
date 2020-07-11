package com.lynbrookrobotics.kapuchin.tests.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.test.Ignore
import kotlin.test.Test

class PathGeneratorTest {

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
                                val a = Waypoint(x1.Metre, y1.Metre)
                                val b = Waypoint(x2.Metre, y2.Metre)

                                val path = nSect(a, b, len)

                                path.size `is greater than or equal to?` 2

                                if (path.size > 2) path
                                    .zipWithNext { p1, p2 -> distance(p1, p2) }
                                    .forEach { len `is greater than or equal to?` it }

                                path.zipWithNext().forEach { (p1, p2) ->
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
    fun `interpolatePath smooths waypoints`() {
        val path = listOf(
            Waypoint(-1.Metre, -1.Metre),
            Waypoint(-1.Metre, 0.Metre),
            Waypoint(1.Metre, 0.Metre),
            Waypoint(1.Metre, 1.Metre)
        )

        val len = 0.1.Metre
        val newPath = interpolatePath(path, len)
        val segs = newPath.zipWithNext()

        segs
            .map { (a, b) -> distance(a, b) }
            .forEach { len * 2 `is greater than or equal to?` it }


        val start = path.first()
        val end = path.last()

        newPath.zipWithNext().forEach { (p1, p2) ->
            val da1 = distance(start, p1)
            val da2 = distance(start, p2)

            val db1 = distance(end, p1)
            val db2 = distance(end, p2)

            da2 `is greater than?` da1
            db1 `is greater than?` db2
        }

        (path - start - end).none { it in newPath } `is equal to?` true
    }

}