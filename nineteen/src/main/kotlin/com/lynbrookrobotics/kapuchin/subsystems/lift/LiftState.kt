package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class LiftState(val rng: ClosedRange<Length>) {

    High(30.Inch..80.Inch),
    Low(3.Inch..16.Inch),
    Bottom(-1.Inch..3.Inch),
    Undetermined(-1.Inch..80.Inch);

    companion object {
        val pos = 1
        val states = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
        operator fun invoke() = Subsystems.instance?.let {
            it.lift?.hardware?.position?.optimizedRead(currentTime, 0.Second)?.y.let {
                if (it == null) {
                    LiftState.Undetermined
                } else {
                    when (it) {
                        in LiftState.Bottom.rng -> LiftState.Bottom
                        in LiftState.Low.rng -> LiftState.Low
                        in LiftState.High.rng -> LiftState.High
                        else -> LiftState.Undetermined
                    }
                }
            }
        }

        fun legalRanges() = Safeties.currentState(lift = LiftState().takeIf { it == LiftState.Undetermined })
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it)?.rng }
    }
}

