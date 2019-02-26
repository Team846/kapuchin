package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class HandoffPivotState(val rng: ClosedRange<Angle>) {

    Vertical(70.Degree..90.Degree),
    High(30.Degree..0.Degree),
    Mid(60.Degree..30.Degree),
    Low(60.Degree..70.Degree),
    Undetermined(0.Degree..70.Degree);

    companion object {
        val pos = 2
        val states = arrayOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
        operator fun invoke() = Subsystems.instance.handoffPivot?.hardware?.position?.optimizedRead(currentTime, 0.Second)?.y.let {
            if (it == null) {
                HandoffPivotState.Undetermined
            } else {
                    when (it) {
                        in HandoffPivotState.High.rng -> HandoffPivotState.High.also { println("HandoffPivotState: High") }
                        in HandoffPivotState.Mid.rng -> HandoffPivotState.Mid.also { println("HandoffPivotState: Mid") }
                        in HandoffPivotState.Low.rng -> HandoffPivotState.Low.also { println("HandoffPivotState: Low") }
                        else -> HandoffPivotState.Undetermined
                }
            }
        }

        fun legalRanges() = Safeties.currentState(handoffPivot = HandoffPivotState().takeIf { it == HandoffPivotState.Undetermined })
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it)?.rng }
    }
}


