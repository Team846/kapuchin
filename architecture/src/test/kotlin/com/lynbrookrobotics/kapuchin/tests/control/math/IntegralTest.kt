package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.integration.FiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import com.lynbrookrobotics.kapuchin.tests.anyInt
import info.kunalsheth.units.generated.Foot
import info.kunalsheth.units.generated.FootPerSecond
import info.kunalsheth.units.generated.Second
import kotlin.test.Test

class IntegralTest {

    @Test
    fun `Finite integral of zero is zero`() {
        anyInt.filter { it > 0 }.map { falloff -> FiniteIntegrator(falloff, 0.Foot) }.forEach { integrator ->
            repeat(50) { time ->
                integrator(time.Second, 0.FootPerSecond) `is equal to?` 0.Foot
            }
        }
    }

    @Test
    fun `Infinite integral of zero is zero`() {
        val integrator = InfiniteIntegrator(0.Foot)
        repeat(50) { time ->
            integrator(time.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Finite integral init is the first value`() {
        anyInt.filter { it > 0 }.map { initValue ->
            anyInt.filter { it > 0 }.map { falloff -> FiniteIntegrator(falloff, initValue.Foot) }.forEach { integrator ->
                integrator(8.46.Second, 84.6.FootPerSecond) `is equal to?` initValue.Foot
            }
        }
    }

    @Test
    fun `Infinite integral init is the first value`() {
        anyDouble.map { initValue ->
            val integrator = InfiniteIntegrator(initValue.Foot)
            integrator(8.46.Second, 84.6.FootPerSecond) `is equal to?` initValue.Foot
        }
    }

    @Test
    fun `Finite integral is finite`() {
        anyInt.filter { it > 1 }.map { falloff -> FiniteIntegrator(falloff, 1.Foot) }.forEach { integrator ->
            val retained = integrator.falloff - 1

            repeat(retained) { time ->
                integrator(time.Second, 0.FootPerSecond) `is equal to?` 1.Foot
            }
            integrator(retained.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Infinite integral is infinite`() {
        val integrator = InfiniteIntegrator(1.Foot)
        repeat(1000) { time ->
            integrator(time.Second, 0.FootPerSecond) `is equal to?` 1.Foot
        }
        integrator(1000.Second, -2.FootPerSecond) `is equal to?` 0.Foot
    }

    @Test
    fun `Finite integral of a positive number is greater than zero`() {
        anyInt.filter { it > 0 }.map { falloff -> FiniteIntegrator(falloff, 1E-5.Foot) }.forEach { integrator ->
            anyDouble.filter { it > 0 }.forEachIndexed { time, value ->
                integrator(time.Second, value.FootPerSecond) `is greater than?` 0.Foot
            }
        }
    }

    @Test
    fun `Infinite integral of a positive number is greater than zero`() {
        val integrator = InfiniteIntegrator(1E-5.Foot)
        anyDouble.filter { it > 0 }.forEachIndexed { time, value ->
            integrator(time.Second, value.FootPerSecond) `is greater than?` 0.Foot
        }
    }
}