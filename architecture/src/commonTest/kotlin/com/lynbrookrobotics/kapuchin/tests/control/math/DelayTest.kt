package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.tests.*
import kotlin.test.Test

class DelayTest {

    @Test
    fun `Delay delays by the previous lookback values`() {
        anyInt.filter { it > 0 }.forEach { falloff ->
            val delay = delay<Int>(falloff)
            val retained = falloff - 1
            repeat(retained) { delay(it) `is equal to?` null }
            repeat(retained) { it `is equal to?` delay(it) }
            repeat(retained) { it `is equal to?` delay(-it) }
            repeat(retained) { -it `is equal to?` delay(0) }
            repeat(retained) { 0 `is equal to?` delay(it) }
        }
    }
}