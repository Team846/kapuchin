package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.data.Delay
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.anyInt
import kotlin.test.Test

class DelayTest {

    @Test
    fun `Delay delays by the previous lookback values`() {
        anyInt.filter { it > 0 }.map { falloff -> Delay<Int>(falloff) }.forEach { delay ->
            val retained = delay.lookBack - 1
            repeat(retained) { delay(it) `is equal to?` null }
            repeat(retained) { it `is equal to?` delay(it) }
            repeat(retained) { it `is equal to?` delay(-it) }
            repeat(retained) { -it `is equal to?` delay(0) }
            repeat(retained) { 0 `is equal to?` delay(it) }
        }
    }
}