package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.electrical.RampRateLimiter
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.generated.VoltPerSecond
import info.kunalsheth.units.generated.milli
import kotlin.test.Test

class ElectricalTests {

    @Test
    fun `ramp rate limiting ramps up and down output`() {
        val startRampUpTime = currentTime

        val rampLimiter = RampRateLimiter(
                12.VoltPerSecond, 0.Volt stampWith startRampUpTime
        )

        val incr = 3.milli(::Second)

        generateSequence(startRampUpTime) { it + incr }
                .takeWhile { it - startRampUpTime < 1.Second }
                .forEach {
                    12.Volt `is greater than?` rampLimiter(it, 12.Volt)
                }
        repeat(50) {
            rampLimiter(startRampUpTime + 1.Second + incr * it, 12.Volt) `is equal to?` 12.Volt
        }

        val startRampDownTime = startRampUpTime + 1.Second + incr * 49

        generateSequence(startRampDownTime) { it + incr }
                .takeWhile { it - startRampDownTime < 2.Second }
                .forEach {
                    val a =rampLimiter(it, -12.Volt)
                    println("t: $it")
                    println(a)

                    a `is greater than?` -12.Volt
                }
        repeat(50) {
            rampLimiter(startRampDownTime + 2.Second + incr * it, -12.Volt) `is equal to?` -12.Volt
        }
    }
}