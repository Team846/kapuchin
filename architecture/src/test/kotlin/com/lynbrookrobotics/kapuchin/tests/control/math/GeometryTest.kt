package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.absoluteValue
import kotlin.test.Test

class GeometryTest {

    @Test
    fun `coterminal difference of the same angle is zero`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                t1.Radian `coterminal -` t1.Radian + n.Turn `is equal to?` 0.Turn
            }
        }
    }

    @Test
    fun `coterminal difference of the differenced angle is the difference`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                anyDouble.filter { it.absoluteValue < 180 }.forEach { dt ->
                    t1.Radian + dt.Degree `coterminal -` t1.Radian + n.Turn `is equal to?` dt.Degree
                }
            }
        }
    }

    @Test
    fun `coterminal difference of 180 is the same angle`() {
        anyInt.forEach { n ->
            abs(180.Degree `coterminal -` n.Turn) `is equal to?` 180.Degree
        }
    }

    @Test
    fun `coterminal difference of -180 is the same angle`() {
        anyInt.forEach { n ->
            abs(-180.Degree `coterminal -` n.Turn) `is equal to?` 180.Degree
        }
    }

    @Test
    fun `coterminal sum of the opposite angle is zero`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                t1.Radian `coterminal +` -t1.Radian + n.Turn `is equal to?` 0.Turn
            }
        }
    }

    @Test
    fun `coterminal sum of the angle is the same angle`() {
        anyInt.forEach { n ->
            anyDouble.filter { it.absoluteValue < 180.0 }.forEach { dt ->
                dt.Degree `coterminal +` n.Turn `is equal to?` dt.Degree
            }
        }
    }

    @Test
    fun `coterminal sum of the opposite differenced angle is the difference`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                anyDouble.filter { it.absoluteValue < 180 }.forEach { dt ->
                    t1.Radian + dt.Degree `coterminal +` -t1.Radian + n.Turn `is equal to?` dt.Degree
                }
            }
        }
    }

    @Test
    fun `coterminal sum of 180 is the same angle`() {
        anyInt.forEach { n ->
            abs(180.Degree `coterminal +` n.Turn) `is equal to?` 180.Degree
        }
    }

    @Test
    fun `coterminal sum of -180 is the same angle`() {
        anyInt.forEach { n ->
            abs(-180.Degree `coterminal +` n.Turn) `is equal to?` 180.Degree
        }
    }

    @Test
    fun `coterminal difference is always within range`() {
        anyDouble.forEach { a ->
            anyDouble.forEach { b ->
                a.Degree `coterminal -` b.Degree `is within?` `±`(180.Degree)
                a.Degree `coterminal -` b.Radian `is within?` `±`(180.Degree)
                a.Radian `coterminal -` b.Degree `is within?` `±`(180.Degree)
                a.Radian `coterminal -` b.Radian `is within?` `±`(180.Degree)
            }
        }
    }

    @Test
    fun `coterminal sum is always within range`() {
        anyDouble.forEach { a ->
            anyDouble.forEach { b ->
                a.Degree `coterminal +` b.Radian `is within?` `±`(180.Degree)
                a.Degree `coterminal +` b.Degree `is within?` `±`(180.Degree)
                a.Radian `coterminal +` b.Degree `is within?` `±`(180.Degree)
                a.Radian `coterminal +` b.Radian `is within?` `±`(180.Degree)
            }
        }
    }
}
