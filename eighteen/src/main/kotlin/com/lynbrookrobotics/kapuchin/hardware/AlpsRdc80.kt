package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.Degree
import info.kunalsheth.units.generated.Turn
import kotlin.math.absoluteValue

class AlpsRdc80(private val phase: Angle) : (Double, Double) -> Angle {

    private val readable = 0.6
    private val mustRead = 0.1

    private val readableRange = 0 withToleranceOf readable
    private val mustReadRange = 0 withToleranceOf mustRead
    private val halfTurn = 0.5.Turn

    private var loopAround = 0.Turn
    private var wasPositive = false
    private var wasNegative = false
    private val posLoopRng = 130.Degree..185.Degree
    private val negLoopRng = -185.Degree..-130.Degree

    override fun invoke(a: Double, b: Double): Angle {
        val angA = (halfTurn * (a + 1)) % 1.Turn - halfTurn
        val angB = (halfTurn * (b + 1) + phase) % 1.Turn - halfTurn

        val weight = when {
            a in mustReadRange || b !in readableRange -> {
                println("a in mustReadRange || b !in readableRange")
                1.0
            }
            b in mustReadRange || a !in readableRange -> {
                println("b in mustReadRange || a !in readableRange")
                0.0
            }
            else -> (b.absoluteValue - mustRead) / (readable - mustRead)
        }

        val angle = angA * weight + angB * (1 - weight)

        if (wasNegative && angle in posLoopRng) loopAround -= 1.Turn
        if (wasPositive && angle in negLoopRng) loopAround += 1.Turn
        wasPositive = angle in posLoopRng
        wasNegative = angle in negLoopRng

        return angle + loopAround
    }
}