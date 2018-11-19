package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.HorizontalDeadband
import com.lynbrookrobotics.kapuchin.control.conversion.deadband.VerticalDeadband
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is within?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import info.kunalsheth.units.generated.Percent
import info.kunalsheth.units.generated.`±`
import kotlin.test.Test

class DeadbandTest {
    @Test
    fun `vertical deadbands are mapped correctly`() {
        anyDouble.filter { it > 0 && it < 100 }.forEach {
            val mapping = VerticalDeadband(it.Percent, 100.Percent)
            val tol = 0.01.Percent

            mapping(0.0.Percent) `is within?` (0.0.Percent `±` tol)
            mapping(tol / 2) `is within?` (it.Percent `±` tol)
            mapping(100.Percent) `is within?` (100.Percent `±` tol)
            mapping(-tol / 2) `is within?` (-it.Percent `±` tol)
            mapping(-100.Percent) `is within?` (-100.Percent `±` tol)
        }
    }

    @Test
    fun `horizontal deadbands are mapped correctly`() {
        anyDouble.filter { it > 0 && it < 100 }.forEach {
            val mapping = HorizontalDeadband(it.Percent, 100.Percent)
            val tol = 0.01.Percent

            mapping(0.0.Percent) `is within?` (0.0.Percent `±` tol)
            mapping(it.Percent / 2) `is within?` (0.0.Percent `±` tol)
            mapping(100.Percent) `is within?` (100.Percent `±` tol)
            mapping(-it.Percent / 2) `is within?` (0.0.Percent `±` tol)
            mapping(-100.Percent) `is within?` (-100.Percent `±` tol)
        }
    }

    @Test
    fun `horizontal deadband is the inverse of vertical deadband`() {
        anyDouble.filter { it > 0 && it < 100 }.forEach {
            val horizontal = HorizontalDeadband(it.Percent, 100.Percent)
            val vertical = VerticalDeadband(it.Percent, 100.Percent)
            anyDouble.forEach {
                horizontal(vertical(it.Percent)) `is equal to?` it.Percent
            }
        }
    }
}