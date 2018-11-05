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
import info.kunalsheth.units.generated.times
import kotlin.test.Test

class IntegralTest {

    @Test
    fun `Finite integral of zero is zero`() {
        anyInt.filter { it > 0 }.forEach { falloff ->
            val integrator = FiniteIntegrator(::times,
                    -Second, 0.FootPerSecond,
                    falloff
            )
            repeat(50) { time ->
                integrator(time.Second, 0.FootPerSecond) `is equal to?` 0.Foot
            }
        }
    }

    @Test
    fun `Infinite integral of zero is zero`() {
        val integrator = InfiniteIntegrator(::times,
                -Second, 0.FootPerSecond
        )
        repeat(50) { time ->
            integrator(time.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Finite integral is finite`() {
        anyInt.filter { it > 1 }.forEach { falloff ->
            val integrator = FiniteIntegrator(::times,
                    -Second, 2.FootPerSecond,
                    falloff
            )

            val retained = integrator.falloff - 1

            repeat(retained) { time ->
                integrator(time.Second, 0.FootPerSecond) `is equal to?` 1.Foot
            }
            integrator(retained.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Infinite integral is infinite`() {
        val integrator = InfiniteIntegrator(::times,
                -Second, 2.FootPerSecond
        )
        repeat(1000) { time ->
            integrator(time.Second, 0.FootPerSecond) `is equal to?` 1.Foot
        }
        integrator(1000.Second, -2.FootPerSecond) `is equal to?` 0.Foot
    }

    @Test
    fun `Finite integral of a positive number is greater than zero`() {
        anyInt.filter { it > 0 }.forEach { falloff ->
            val integrator = FiniteIntegrator(::times,
                    -Second, 1E-5.FootPerSecond,
                    falloff
            )
            anyDouble.filter { it > 0 }.forEachIndexed { time, value ->
                integrator(time.Second, value.FootPerSecond) `is greater than?` 0.Foot
            }
        }
    }

    @Test
    fun `Infinite integral of a positive number is greater than zero`() {
        val integrator = InfiniteIntegrator(::times,
                -Second, 1E-5.FootPerSecond
        )
        anyDouble.filter { it > 0 }.forEachIndexed { time, value ->
            integrator(time.Second, value.FootPerSecond) `is greater than?` 0.Foot
        }
    }
}