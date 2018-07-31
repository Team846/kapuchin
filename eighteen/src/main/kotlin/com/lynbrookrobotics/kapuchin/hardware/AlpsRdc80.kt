package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.plusOrMinus
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Degree
import info.kunalsheth.units.generated.Turn
import kotlin.math.absoluteValue

class AlpsRdc80(private val phase: Angle) : (Double, Double) -> Angle {

    private val readableRange = 0 plusOrMinus 0.7
    private val halfTurn = 0.5.Turn

    private var loopAround = 0.Turn
    private var wasPositive = false
    private var wasNegative = false
    private val posLoopRng = 160.Degree..180.Degree
    private val negLoopRng = -180.Degree..-160.Degree

    override fun invoke(a: Double, b: Double): Angle {
        val angA = (halfTurn * (a + 1)) % 1.Turn - halfTurn
        val angB = (halfTurn * (b + 1) + phase) % 1.Turn - halfTurn

        val weightA = weight(a)
        val weightB = weight(b)

        val angle = (angA * weightA + angB * weightB) / (weightA + weightB)

        if (wasNegative && angle in posLoopRng) loopAround -= 1.Turn
        if (wasPositive && angle in negLoopRng) loopAround += 1.Turn
        wasPositive = angle in posLoopRng
        wasNegative = angle in negLoopRng

        return angle + loopAround
    }

    private fun weight(x: Double) =
            if (x in readableRange) readableRange.endInclusive - x.absoluteValue
            else 0.0
}