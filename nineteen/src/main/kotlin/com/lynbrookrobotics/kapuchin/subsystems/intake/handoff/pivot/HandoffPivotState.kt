package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class HandoffPivotState(val rng: ClosedRange<Angle>) {

    Vertical(45.Degree..110.Degree),
    High(40.Degree..45.Degree),
    Mid(30.Degree..40.Degree),
    Low(-5.Degree..30.Degree),
    Undetermined(-5.Degree..110.Degree);

    companion object {
        val states = arrayOf(HandoffPivotState.Vertical, HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
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

        fun legalRanges(): List<ClosedRange<Angle>> {
            val (legal, illegal) = Safeties.currentState(handoffPivot = HandoffPivotState().takeIf { it == Undetermined })
                    .partition { it !in Safeties.illegalStates }
            val mappedLegal = legal.mapNotNull { decode(it)?.rng }
            val mappedIllegal = illegal.mapNotNull { decode(it)?.rng }


            return when {
                mappedLegal.isEmpty() -> mappedIllegal
                else -> mappedLegal
            }
        }
    }
}


