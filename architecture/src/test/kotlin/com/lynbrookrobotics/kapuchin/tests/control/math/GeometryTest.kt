package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.math.absoluteValue
import kotlin.test.Test

class GeometryTest {

    @Test
    fun `coterminal difference of the same angle is zero`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                t1.Radian `coterminal -` t1.Radian + (2 * n).Turn `is equal to?` 0.Turn
            }
        }
    }

    @Test
    fun `coterminal difference of the differenced angle is the difference`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                anyDouble.filter { it.absoluteValue < 360 }.forEach { dt ->
                    t1.Radian + dt.Degree `coterminal -` t1.Radian + n.Turn `is equal to?` dt.Degree
                }
            }
        }
    }
}
