package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.test.Test

class ConversionTest {
    @Test
    fun `encoder ticks and angle methods are inverses`() {
        anyInt.filter { it != 0 }.map { resolution -> EncoderConversion(resolution, 360.Degree) }
                .forEach { conversion ->
                    anyDouble.map { it.Each }.forEach { x ->
                        x.Each `is equal to?` (conversion.ticks(conversion.angle(x.Each)) withDecimals 5)

                        val ix = x * t
                        ix `is equal to?` conversion.ticks(conversion.angle(ix))

                        val dx = x / t
                        dx `is equal to?` conversion.ticks(conversion.angle(dx))

                        val ddx = dx / t
                        ddx `is equal to?` conversion.ticks(conversion.angle(ddx))
                    }
                }
    }

    @Test
    fun `offloaded real and native methods are inverses`() {
        anyInt.filter { it != 0 }.map { resolution ->
            LinearOffloadedNativeConversion(
                    ::p, ::p, ::p, ::p,
                    1023, 12.Volt, resolution, 8.46.Metre, 1.Foot,
                    nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )
        }.forEach { conversion ->
            anyDouble.map { it.Foot }.forEach { x ->
                x `is equal to?` conversion.realPosition(conversion.native(x))

                val dx = x / t
                dx `is equal to?` conversion.realVelocity(conversion.native(dx))
            }
        }
    }

    @Test
    fun `offloaded native methods are linear`() {
        anyInt.filter { it != 0 }.map { resolution ->
            OffloadedNativeConversion<V, Absement, Length, Velocity, Acceleration>(
                    ::p, ::p, ::p, ::p,
                    1023, 12.Volt, resolution, 8.46.Metre,
                    nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
            )
        }.forEach { conversion ->
            anyDouble.map { it.Foot }.forEach { x ->
                conversion.native(-x) * 2 `is equal to?` -conversion.native(x * 2)
                conversion.native(Gain(20.Volt, x)) `is equal to?` conversion.native(Gain(10.Volt, x)) * 2

                val ix = x * Second
                conversion.native(-ix) * 2 `is equal to?` -conversion.native(ix * 2)
                conversion.native(Gain(20.Volt, ix)) `is equal to?` conversion.native(Gain(10.Volt, ix)) * 2

                val dx = x / Second
                conversion.native(-dx) * 2 `is equal to?` -conversion.native(dx * 2)
                conversion.native(Gain(20.Volt, dx)) `is equal to?` conversion.native(Gain(10.Volt, dx)) * 2

                val ddx = dx / t
                conversion.native(-ddx) * 2 `is equal to?` -conversion.native(ddx * 2)
                conversion.native(Gain(20.Volt, ddx)) `is equal to?` conversion.native(Gain(10.Volt, ddx)) * 2
            }

            anyDouble.map { it.Volt }.forEach { x ->
                conversion.native(-x) * 2 `is equal to?` -conversion.native(x * 2)
            }
        }
    }

    @Test
    fun `gears input and output methods are inverses`() {
        anyInt.filter { it > 0 }.forEach { input ->
            anyInt.filter { it > 0 }.forEach { idlers ->
                anyInt.filter { it > 0 }
                        .map { output -> GearTrain(input, output, idlers) }
                        .forEach { gearTrain ->
                            anyDouble.map { it.Degree }.forEach { x ->
                                x `is equal to?` gearTrain.outputToInput(gearTrain.inputToOutput(x))

                                val dx = x / t
                                dx `is equal to?` gearTrain.outputToInput(gearTrain.inputToOutput(dx))

                                val ddx = dx / t
                                ddx `is equal to?` gearTrain.outputToInput(gearTrain.inputToOutput(ddx))
                            }
                        }
            }
        }
    }
}