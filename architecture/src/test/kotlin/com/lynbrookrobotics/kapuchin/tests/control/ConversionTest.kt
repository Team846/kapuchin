package com.lynbrookrobotics.kapuchin.tests.control

import com.lynbrookrobotics.kapuchin.control.conversion.EncoderConversion
import com.lynbrookrobotics.kapuchin.control.conversion.SimpleGearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.TalonNativeConversion
import com.lynbrookrobotics.kapuchin.control.conversion.WheelConversion
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import com.lynbrookrobotics.kapuchin.tests.anyInt
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class ConversionTest {
    private val t = 1.Second

    @Test
    fun `encoder ticks and angle methods should be inverses`() {
        anyInt.filter { it != 0 }.map { resolution -> EncoderConversion(resolution.Tick, 360.Degree) }
                .forEach { conversion ->
                    anyDouble.map { it.Tick }.forEach { x ->
                        x `is equal to?` conversion.ticks(conversion.angle(x))

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
    fun `wheel length and angle methods should be inverses`() {
        anyInt.filter { it > 0 }
                .map { radius -> WheelConversion(radius.Inch) }
                .forEach { conversion ->
                    anyDouble.map { it.Foot }.forEach { x ->
                        x `is equal to?` conversion.length(conversion.angle(x))

                        val ix = x * t
                        ix `is equal to?` conversion.length(conversion.angle(ix))

                        val dx = x / t
                        dx `is equal to?` conversion.length(conversion.angle(dx))

                        val ddx = dx / t
                        ddx `is equal to?` conversion.length(conversion.angle(ddx))
                    }
                }
    }

    @Test
    fun `talon real and native methods should be inverses`() {
        anyInt.filter { it != 0 }.map { resolution -> TalonNativeConversion(resolution.Tick, 8.46.Metre) }
                .forEach { conversion ->
                    anyDouble.map { it.Foot }.forEach { x ->
                        x `is equal to?` conversion.realPosition(conversion.native(x))

                        val dx = x / t
                        dx `is equal to?` conversion.realVelocity(conversion.native(dx))
                    }
                }
    }

    @Test
    fun `gears input and output methods should be inverses`() {
        anyInt.filter { it > 0 }.forEach { input ->
            anyInt.filter { it > 0 }.forEach { idlers ->
                anyInt.filter { it > 0 }
                        .map { output -> SimpleGearTrain(input, idlers, output) }
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