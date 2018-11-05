package com.lynbrookrobotics.kapuchin.tests.control.loops

import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidControlLoop
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.anyInt
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class PidTest {

    @Test
    fun `pid kP is proportional`() {
        val pid = PidControlLoop(::div, ::times, PidGains(
                Gain(6.Volt, 1.Foot),
                Gain(0.Volt, 1.FootSecond),
                Gain(0.Volt, 1.FootPerSecond)
        )) { _ -> 0.Foot }

        val dt = 200.milli(Second)
        repeat(20) { time ->
            pid(time.Second + dt * 0, -2.Foot) `is equal to?` 12.Volt
            pid(time.Second + dt * 1, -1.Foot) `is equal to?` 6.Volt
            pid(time.Second + dt * 2, 0.Foot) `is equal to?` 0.Volt
            pid(time.Second + dt * 3, 1.Foot) `is equal to?` -6.Volt
            pid(time.Second + dt * 4, 2.Foot) `is equal to?` -12.Volt
        }
    }

    @Test
    fun `pid kI is integral`() {
        val pid = PidControlLoop(::div, ::times, PidGains(
                Gain(0.Volt, 1.Foot),
                Gain(6.Volt, 1.FootSecond),
                Gain(0.Volt, 1.FootPerSecond)
        )) { _ -> 0.Foot }

        val acc = Array(10) { time ->
            pid(time.Second, -Foot)
        }

        val shift = 10.Second
        val dec = Array(10) { time ->
            pid(time.Second + shift, 1.Foot)
        }

        dec.reversedArray().zip(acc).forEach { (a, b) ->
            a `is equal to?` b
        }
    }

    @Test
    fun `pid kD is derivative`() {
        val pid = PidControlLoop(::div, ::times, PidGains(
                Gain(0.Volt, 1.Foot),
                Gain(0.Volt, 1.FootSecond),
                Gain(6.Volt, 1.FootPerSecond)
        )) { _ -> 0.Foot }

        val inputs = Array(10) { time ->
            (time * time).Foot
        }

        val acc = inputs.mapIndexed { time, value ->
            pid(time.Second, value)
        }.drop(1)

        val shift = 10.Second
        val dec = inputs.reversed().mapIndexed { time, value ->
            pid(time.Second + shift, value)
        }.drop(1)

        acc.reversed().zip(dec).forEach { (a, b) ->
            a `is equal to?` -b
        }
    }

    @Test
    fun `pid kF is feedforward`() {
        val pid = PidControlLoop(::div, ::times, PidGains(
                Gain(1.Volt, 1.FootPerSecond),
                Gain(0.Volt, 1.Foot),
                Gain(0.Volt, 1.FootPerSecondSquared),
                Gain(6.Volt, 1.FootPerSecond)
        )) { _ -> 10.FootPerSecond }

        val dt = 200.milli(Second)
        repeat(20) { time ->
            pid(time.Second + dt * 0, 8.FootPerSecond) `is equal to?` 62.Volt
            pid(time.Second + dt * 1, 9.FootPerSecond) `is equal to?` 61.Volt
            pid(time.Second + dt * 2, 10.FootPerSecond) `is equal to?` 60.Volt
            pid(time.Second + dt * 3, 11.FootPerSecond) `is equal to?` 59.Volt
            pid(time.Second + dt * 4, 12.FootPerSecond) `is equal to?` 58.Volt
        }
    }

    @Test
    fun `pid integral falloff caps integral error`() {
        anyInt.filter { it > 0 }.forEach { falloff ->
            val pid = PidControlLoop(::div, ::times, PidGains(
                    Gain(6.Volt, 1.Foot),
                    Gain(1.Volt, 1.FootSecond),
                    Gain(2.Volt, 1.FootPerSecond),
                    null,
                    falloff
            )) { _ -> 0.Foot }

            repeat(falloff) { time ->
                pid(time.Second, -Foot)
            }
            pid(falloff.Second, -Foot) `is equal to?` pid((falloff + 1).Second, -Foot)
        }
    }
}