package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.Hysteresis
import com.lynbrookrobotics.kapuchin.control.plusOrMinus
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import info.kunalsheth.units.generated.Percent
import kotlin.test.Test

class HysteresisTest {
    @Test
    fun `hysteresis should only toggle when at extremes of range`() {
        val h = Hysteresis(50.Percent plusOrMinus 5.Percent)

        repeat(55) {
            h(it.Percent) `is equal to?` false
        }

        repeat(55) {
            h(55.Percent - it.Percent) `is equal to?` false
        }

        repeat(55) {
            h(100.Percent - it.Percent) `is equal to?` true
        }

        repeat(55) {
            h(45.Percent + it.Percent) `is equal to?` true
        }

        h(10.Percent) `is equal to?` false
        h(90.Percent) `is equal to?` true
        h(50.Percent) `is equal to?` true

        h(90.Percent) `is equal to?` true
        h(10.Percent) `is equal to?` false
        h(50.Percent) `is equal to?` false
    }
}