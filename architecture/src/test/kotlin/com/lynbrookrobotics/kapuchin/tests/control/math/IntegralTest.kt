package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class IntegralTest {

    @Test
    fun `Finite integral of zero is zero`() {
        anyInt.filter { it > 0 }.forEach { falloff ->
            val integrator = finiteIntegrator(::p,
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
        val integrator = infiniteIntegrator(::p,
                -Second, 0.FootPerSecond
        )
        repeat(50) { time ->
            integrator(time.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Finite integral is finite`() {
        anyInt.filter { it > 1 }.forEach { falloff ->
            val integrator = finiteIntegrator(::p,
                    -Second, 2.FootPerSecond,
                    falloff
            )

            val retained = falloff - 1

            repeat(retained) { time ->
                integrator(time.Second, 0.FootPerSecond) `is equal to?` 1.Foot
            }
            integrator(retained.Second, 0.FootPerSecond) `is equal to?` 0.Foot
        }
    }

    @Test
    fun `Infinite integral is infinite`() {
        val integrator = infiniteIntegrator(::p,
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
            val integrator = finiteIntegrator(::p,
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
        val integrator = infiniteIntegrator(::p,
                -Second, 1E-5.FootPerSecond
        )
        anyDouble.filter { it > 0 }.forEachIndexed { time, value ->
            integrator(time.Second, value.FootPerSecond) `is greater than?` 0.Foot
        }
    }
}