package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.`coterminal -`
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import com.lynbrookrobotics.kapuchin.tests.anyInt
import info.kunalsheth.units.generated.Degree
import info.kunalsheth.units.generated.Radian
import info.kunalsheth.units.generated.Turn
import kotlin.test.Test

class GeometryTest {

    @Test
    fun `coterminal difference of the same angle is zero`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                t1.Radian `coterminal -` t1.Radian + n.Turn `is equal to?` 0.Degree
            }
        }
    }

    @Test
    fun `coterminal difference of the differenced angle is the difference`() {
        anyDouble.forEach { t1 ->
            anyInt.forEach { n ->
                anyDouble.forEach { dt ->
                    t1.Radian + dt.Degree `coterminal -` t1.Radian + n.Turn `is equal to?` dt.Degree
                }
            }
        }
    }
}
