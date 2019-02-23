package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.LiftState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.lift.LiftState.Companion.states
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlin.math.pow

sealed class LiftState(val rng: ClosedRange<Length>) {
    object High : LiftState(30.Inch..80.Inch)
    object Low : LiftState(3.Inch..16.Inch)
    object Bottom : LiftState(-1.Inch..3.Inch)

    companion object {
        val pos = 1
        val states = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
        operator fun invoke() = Subsystems.lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in LiftState.Bottom.rng -> LiftState.Bottom
                in LiftState.Low.rng -> LiftState.Low
                in LiftState.High.rng -> LiftState.High
                else -> null
            }
        }

    }
}


fun LiftState.encode(): Int {
    val index = states.indexOf(this)
    return if (index >= 0) index * 10.0.pow(pos - 1) as Int else throw Throwable("Unknown lift state encountered")
}

private fun LiftComponent.decode(state: RobotState): LiftState? {
    val index = state.code / (10.0.pow(pos) as Int) % 10
    return states[index]
}

fun LiftComponent.legalRanges() = Safeties.currentState(lift = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }
