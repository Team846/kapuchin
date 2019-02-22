package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

sealed class HandoffPivotState(val rng: ClosedRange<Angle>, val code: Int) {
    object High : HandoffPivotState(30.Degree..0.Degree, 0b00_00_000_0_0)
    object Mid : HandoffPivotState(60.Degree..30.Degree, 0b00_01_000_0_0)
    object Low : HandoffPivotState(90.Degree..60.Degree, 0b00_10_000_0_0)

    companion object {
        val handoffPivotQueryCode = 0b00_11_000_0_0
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

val handoffPivotStates = arrayOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
private fun HandoffPivotComponent.decode(state: RobotState): HandoffPivotState? {
    val handoffCode = state.code and HandoffPivotState.handoffPivotQueryCode
    return handoffPivotStates.find {it.code == handoffCode }
}
fun HandoffPivotComponent.legalRanges() = Safeties.currentState(handoffPivot = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }