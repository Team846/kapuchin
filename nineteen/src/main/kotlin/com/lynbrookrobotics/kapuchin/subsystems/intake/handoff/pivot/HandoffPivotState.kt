package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.HandoffPivotState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.HandoffPivotState.Companion.states
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlin.math.pow

sealed class HandoffPivotState(val rng: ClosedRange<Angle>, val code: Int) {
    object High : HandoffPivotState(30.Degree..0.Degree, 0b00_00_000_0_0)
    object Mid : HandoffPivotState(60.Degree..30.Degree, 0b00_01_000_0_0)
    object Low : HandoffPivotState(90.Degree..60.Degree, 0b00_10_000_0_0)

    companion object {
        val pos = 2
        val states = arrayOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
        operator fun invoke() = Subsystems.handoffPivot.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in HandoffPivotState.High.rng -> HandoffPivotState.High
                in HandoffPivotState.Mid.rng -> HandoffPivotState.Mid
                in HandoffPivotState.Low.rng -> HandoffPivotState.Low
                else -> null
            }
        }
    }
}


fun HandoffPivotState.encode(): Int {
    val index = states.indexOf(this)
    return if (index >= 0) index * 10.0.pow(pos - 1) as Int else throw Throwable("Unknown state encountered")
}

private fun HandoffPivotComponent.decode(state: RobotState): HandoffPivotState? {
    val index = state.code / (10.0.pow(pos) as Int) % 10
    return states[index]
}
fun HandoffPivotComponent.legalRanges() = Safeties.currentState(handoffPivot = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }
