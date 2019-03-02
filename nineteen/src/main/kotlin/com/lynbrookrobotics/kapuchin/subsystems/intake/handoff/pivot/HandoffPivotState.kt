package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

sealed class HandoffPivotState(val rng: ClosedRange<Angle>) {
    object Vertical : HandoffPivotState(70.Degree..90.Degree)
    object High : HandoffPivotState(30.Degree..0.Degree)
    object Mid : HandoffPivotState(60.Degree..30.Degree)
    object Low : HandoffPivotState(60.Degree..70.Degree)

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
        fun legalRanges() = Safeties.currentState(handoffPivot = null)
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it)?.rng }
    }
}


