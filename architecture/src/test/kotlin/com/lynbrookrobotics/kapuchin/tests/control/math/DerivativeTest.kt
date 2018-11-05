package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import info.kunalsheth.units.generated.Foot
import info.kunalsheth.units.generated.FootPerSecond
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.div
import kotlin.test.Test

class DerivativeTest {
    @Test
    fun `Derivative of constant is zero`() {
        anyDouble.forEach { constant ->
            val differentiator = Differentiator(::div,
                    -Second, constant.Foot
            )
            repeat(50) { time ->
                differentiator(time.Second, constant.Foot) `is equal to?` 0.FootPerSecond
            }
        }
    }

    @Test
    fun `Derivative of a increasing numbers is greater than zero`() {
        val differentiator = Differentiator(::div,
                -Second, (anyDouble.min()!! - 1).Foot
        )
        anyDouble.sorted().forEachIndexed { time, increasingValue ->
            differentiator(time.Second, increasingValue.Foot) `is greater than?` 0.FootPerSecond
        }
    }
}