package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class HandoffPivotState(val rng: ClosedRange<Angle>) {

    Vertical(45.Degree..90.Degree),
    High(40.Degree..45.Degree),
    Mid(30.Degree..40.Degree),
    Low(-5.Degree..30.Degree),
    Undetermined(-5.Degree..90.Degree);

    companion object {
        val pos = 2
        val states = arrayOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
        operator fun invoke() = Subsystems.instance?.let {
            it.handoffPivot?.hardware?.position?.optimizedRead(currentTime, 0.Second)?.y.let {
                if (it == null) {
                    HandoffPivotState.Undetermined
                } else {
                    when (it) {
                        in HandoffPivotState.High.rng -> HandoffPivotState.High
                        in HandoffPivotState.Mid.rng -> HandoffPivotState.Mid
                        in HandoffPivotState.Low.rng -> HandoffPivotState.Low
                        in HandoffPivotState.Vertical.rng -> HandoffPivotState.Vertical
                        else -> HandoffPivotState.Undetermined
                    }
                }
            }
        }

        fun legalRanges() = Safeties.currentState(handoffPivot = HandoffPivotState().takeIf { it == HandoffPivotState.Undetermined })
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it)?.rng }
    }
}


