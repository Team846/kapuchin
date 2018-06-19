package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import info.kunalsheth.units.generated.Foot
import info.kunalsheth.units.generated.FootPerSecond
import info.kunalsheth.units.generated.Second
import kotlin.test.Test

class DerivativeTest {
    @Test
    fun `Derivative of constant is zero`() {
        anyDouble.forEach { constant ->
            val differentiator = Differentiator(0.FootPerSecond)
            repeat(50) { time ->
                differentiator(time.Second, constant.Foot) `is equal to?` 0.FootPerSecond
            }
        }
    }

    @Test
    fun `Derivative init is the first value`() {
        anyDouble.forEach { initValue ->
            val differentiator = Differentiator(initValue.FootPerSecond)
            differentiator(8.46.Second, 84.6.Foot) `is equal to?` initValue.FootPerSecond
        }
    }

    @Test
    fun `Derivative of a increasing numbers is greater than zero`() {
        val differentiator = Differentiator(1E-5.FootPerSecond)
        anyDouble.sorted().forEachIndexed { time, increasingValue ->
            differentiator(time.Second, increasingValue.Foot) `is greater than?` 0.FootPerSecond
        }
    }
}