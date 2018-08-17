package com.lynbrookrobotics.kapuchin.hardware

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Degree
import info.kunalsheth.units.generated.Each
import info.kunalsheth.units.generated.Turn
import kotlin.math.max

//phase is the angle you rotate B clockwise to get A
class AlpsRdc80(private val phase: Angle) : (Double, Double) -> Angle {

    val truePhase = max(phase.Degree, 360 - phase.Degree)

    val switchedA = if (truePhase != phase.Degree) true else false

    val range = 0.2 //36 degrees

    val defaultBottom = truePhase / 2 * 180
    val defaultTop = (360 - truePhase) / 2 * 180

    val rangeA = -(defaultTop + range)..defaultBottom + range
    val rangeB = -(defaultBottom + range)..defaultTop + range

    val weightedPositiveRangeA = defaultBottom..defaultBottom + range
    val weightedNegativeRangeA = -(defaultTop + range)..defaultTop

    val weightedPositiveRangeB = defaultTop..defaultTop + range
    val weightedNegativeRangeB = -(defaultBottom + range)..defaultBottom

    private val halfTurn = 0.5.Turn

    private var readingA = true

    private var loopAround = 0.Turn
    private var wasPositive = false
    private var wasNegative = false
    private val posLoopRng = 130.Degree..185.Degree
    private val negLoopRng = -185.Degree..-130.Degree

    //a, b are values between -1.0 and 1.0
    override fun invoke(a: Double, b: Double): Angle {
        val angA = (halfTurn * (a + 1)) % 1.Turn - halfTurn
        val angB = (halfTurn * (b + 1) + phase) % 1.Turn - halfTurn


        //true -> reading A
        //false -> reading B
        readingA = when {
            readingA && a !in rangeA -> false
            !readingA && b !in rangeB -> true
            else -> readingA
        }

        val weightAssumingA = when {
            a in weightedPositiveRangeA ->
                (weightedPositiveRangeA.endInclusive - a) / (weightedPositiveRangeA.endInclusive - weightedPositiveRangeA.start)
            a in weightedNegativeRangeA ->
                (a - weightedNegativeRangeA.start) / (weightedNegativeRangeA.start - weightedNegativeRangeA.endInclusive)
            else -> 1.0
        }

        val weightAssumingB = when {
            b in weightedPositiveRangeB ->
                1 - (weightedPositiveRangeB.endInclusive - b) / (weightedPositiveRangeB.endInclusive - weightedPositiveRangeB.start)
            b in weightedNegativeRangeA ->
                1 - (b - weightedNegativeRangeB.start) / (weightedNegativeRangeB.start - weightedNegativeRangeB.endInclusive)
            else -> 0.0
        }

        val weight = when (readingA) {
            true -> {
                when (switchedA) {
                    true -> weightAssumingB
                    false -> weightAssumingA
                }
            }
            (false) -> {
                when (switchedA) {
                    true -> weightAssumingA
                    false -> weightAssumingB
                }
            }
        }

        val angle = if (readingA) angA * weight else angB * (1 - weight)


        if (wasNegative && angle in posLoopRng) loopAround -= 1.Turn
        if (wasPositive && angle in negLoopRng) loopAround += 1.Turn
        wasPositive = angle in posLoopRng
        wasNegative = angle in negLoopRng

        return angle + loopAround
    }
}