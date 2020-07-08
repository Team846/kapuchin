package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class DerivativeTest {
    @Test
    fun `Derivative of constant is zero`() {
        anyDouble.forEach { constant ->
            val differentiator = differentiator(::p,
                    -Second, constant.Foot
            )
            repeat(50) { time ->
                differentiator(time.Second, constant.Foot) `is equal to?` 0.FootPerSecond
            }
        }
    }

    @Test
    fun `Derivative of a increasing numbers is greater than zero`() {
        val differentiator = differentiator(::p,
                -Second, (anyDouble.min()!! - 1).Foot
        )
        anyDouble.sorted().forEachIndexed { time, increasingValue ->
            differentiator(time.Second, increasingValue.Foot) `is greater than?` 0.FootPerSecond
        }
    }
}