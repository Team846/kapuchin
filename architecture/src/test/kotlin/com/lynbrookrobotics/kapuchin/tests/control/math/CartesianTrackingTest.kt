package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.control.math.CartesianTrackingTest.Direction.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test

class CartesianTrackingTest {
    // In 2019, we call HighFrequencyTracking every 0.03 mm
    @Test
    fun `high frequency tracking rotation`() {
        val tracking = HighFrequencyTracking(2.Foot, Position(8.Foot, 4.Foot, 6.Degree))
        anyDouble.shuffled(Random(846))
                .filter { it.absoluteValue > 0.1 }
                .map { 0.03 + 1 / it }
                .forEach {
                    val sl = it.milli(Metre)
                    val sr = -sl

                    tracking(sl, sr)

                    tracking.x `is within?` (8.Foot `±` 0.1.Inch)
                    tracking.y `is within?` (4.Foot `±` 0.1.Inch)
                }
    }

    @Test
    fun `circular arc tracking rotation`() {
        val tracking = CircularArcTracking(Position(8.Foot, 4.Foot, 6.Degree))
        anyDouble.shuffled(Random(846))
                .forEach {
                    val sl = it.milli(Metre)
                    val sr = -sl

                    tracking(sl, sr, theta(sl, sr, 2.Foot))
                    val x = tracking.x
                    val y = tracking.y
                    x `is within?` (8.Foot `±` 0.1.Inch)
                    y `is within?` (4.Foot `±` 0.1.Inch)
                }
    }

    @Test
    fun `high frequency tracking go forward 5 feet`() {
        var sum = 0.Foot
        val tracking = HighFrequencyTracking(2.Foot, Position(8.Foot, 4.Foot, -45.Degree))

        while (sum < 5.Foot) {
            anyDouble.filter { it.absoluteValue > 1.0 }.forEach {
                val dist = 0.03.milli(Metre) + (0.1 / it).milli(Metre)
                tracking(dist, dist)
                sum += dist
            }
        }

        tracking(5.Foot - sum, 5.Foot - sum)

        tracking.x `is within?` (4.465.Foot `±` 0.1.Inch)
        tracking.y `is within?` (7.535.Foot `±` 0.1.Inch)
        tracking.bearing `is equal to?` -45.Degree
    }

    @Test
    fun `circular arc tracking go forward 5 feet`() {
        var sum = 0.Foot
        val tracking = CircularArcTracking(Position(8.Foot, 4.Foot, -45.Degree))

        while (sum < 5.Foot) {
            anyDouble.filter { it.absoluteValue > 1.0 }.forEach {
                val dist = 1.centi(Metre) + (0.1 / it).centi(Metre)
                tracking(dist, dist, -45.Degree)
                sum += dist
            }
        }

        tracking(5.Foot - sum, 5.Foot - sum, -45.Degree)

        tracking.x `is within?` (4.465.Foot `±` 0.1.Inch)
        tracking.y `is within?` (7.535.Foot `±` 0.1.Inch)
        tracking.bearing `is equal to?` -45.Degree
    }

    @Test
    fun `high frequency tracking figure 8`() {
        var pos = Position(0.Foot, 0.Foot, 0.Degree)

        pos = highFreqArc(pos, 10.Foot, 180.Degree, Left)
        pos = highFreqArc(pos, 10.Foot, 180.Degree, Right)
        pos = highFreqArc(pos, 10.Foot, 180.Degree, Right)
        pos = highFreqArc(pos, 10.Foot, 180.Degree, Left)

        pos.x `is within?` (0.Foot `±` 0.1.Inch)
        pos.y `is within?` (0.Foot `±` 0.1.Inch)
        pos.bearing `is within?` (0.Degree `±` 1.Degree)
    }

    @Test
    fun `circular arc tracking figure 8`() {
        var pos = Position(0.Foot, 0.Foot, 0.Degree)

        pos = circArcArc(pos, 10.Foot, 180.Degree, Left)
        pos = circArcArc(pos, 10.Foot, 180.Degree, Right)
        pos = circArcArc(pos, 10.Foot, 180.Degree, Right)
        pos = circArcArc(pos, 10.Foot, 180.Degree, Left)

        pos.x `is within?` (0.Foot `±` 1.Inch)
        pos.y `is within?` (0.Foot `±` 1.Inch)
        pos.bearing `is within?` (0.Degree `±` 1.Degree)
    }

    @Test
    fun `all tracking algorithms behave identically`() {
        val track = 2.Foot

        val init = Position(-8.Foot, 46.Foot, -84.6.Degree)

        val svet = SimpleVectorTracking(track, init)
        val svit = SimpleVectorTracking(track, init)
        val cat = CircularArcTracking(init)
        val hft = HighFrequencyTracking(track, init)

        var Σθ = init.bearing

        val random = Random(846)
        val sideTravelLengths = anyDouble.map { it.milli(Metre) }.filter { it.abs < 6.Inch }

        sideTravelLengths.shuffled(random).forEach { sl ->
            sideTravelLengths.shuffled(random).forEach { sr ->
                Σθ += theta(sl, sr, track)

                svet(sl, sr, Σθ)
                svit(sl, sr, null)
                cat(sl, sr, Σθ)
                repeat(10) { hft(sl / 10, sr / 10) }

                svit.x `is within?` (svet.x `±` 1.Inch)
                svit.y `is within?` (svet.y `±` 1.Inch)
                svit.bearing `coterminal -` Σθ `is within?` `±`(1.Degree)

                cat.y `is within?` (svet.y `±` 8.Inch)
                cat.x `is within?` (svet.x `±` 8.Inch)
                cat.bearing `coterminal -` Σθ `is within?` `±`(1.Degree)

                hft.x `is within?` (svet.x `±` 8.Inch)
                hft.y `is within?` (svet.y `±` 8.Inch)
                hft.bearing `coterminal -` Σθ `is within?` `±`(1.Degree)

            }
        }
    }

    enum class Direction {
        Left, Right
    }

    private fun highFreqArc(init: Position, r: Length, angle: Angle, direction: Direction): Position {
        var sum = 0.Degree

        val tracking = HighFrequencyTracking(2.Foot, init)

        while (sum < angle) {
            anyDouble.filter { it > 1.0 }.forEach { constant ->
                val dist = 0.03.milli(Metre) + (0.1 / constant).milli(Metre)
                val theta = dist / r * Radian

                val track = when (direction) {
                    Left -> 1.Foot
                    Right -> -1.Foot
                }

                val distl = theta * (r - track) / Radian
                val distr = theta * (r + track) / Radian

                sum += theta

                tracking(distl, distr)
            }
        }

        return Position(tracking.x, tracking.y, tracking.bearing)
    }

    private fun circArcArc(init: Position, r: Length, angle: Angle, direction: Direction): Position {
        var sum = 0.Degree

        val tracking = CircularArcTracking(init)

        while (sum < angle) {
            anyDouble.filter { it.absoluteValue > 1.0 }.forEach { constant ->
                val dist = 1.milli(Metre) + (1 / constant).milli(Metre)
                val theta = dist / r * Radian

                val track = when (direction) {
                    Left -> -1.Foot
                    Right -> +1.Foot
                }

                val distl = theta * (r + track) / Radian
                val distr = theta * (r - track) / Radian

                tracking(distl, distr, init.bearing + sum * track.signum)
                sum += theta
            }
        }

        return Position(tracking.x, tracking.y, tracking.bearing)
    }
}