package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.test.Test

class DeadbandTest {
    @Test
    fun `vertical deadbands are mapped correctly`() {
        anyDouble.filter { it > 0 && it < 100 }.forEach {
            val mapping = verticalDeadband(it.Percent, 100.Percent)
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
            val mapping = horizontalDeadband(it.Percent, 100.Percent)
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
            val horizontal = horizontalDeadband(it.Percent, 100.Percent)
            val vertical = verticalDeadband(it.Percent, 100.Percent)
            anyDouble.forEach {
                horizontal(vertical(it.Percent)) `is equal to?` it.Percent
            }
        }
    }
}