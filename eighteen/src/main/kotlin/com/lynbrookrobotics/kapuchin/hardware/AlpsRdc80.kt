package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.plusOrMinus
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Turn
import kotlin.math.absoluteValue

class AlpsRdc80(private val phase: Angle) : (Double, Double) -> Angle {

    private val readableRange = 0 plusOrMinus 0.7
    private val halfTurn = 0.5.Turn

    override fun invoke(a: Double, b: Double): Angle {
        val angA = (halfTurn * a) % halfTurn
        val angB = (halfTurn * b + phase) % halfTurn

        val weightA = weight(a)
        val weightB = weight(b)

        return (angA * weightA + angB * weightB) / (weightA + weightB)
    }

    private fun weight(x: Double) =
            if (x in readableRange) readableRange.endInclusive - x.absoluteValue
            else 0.0
}