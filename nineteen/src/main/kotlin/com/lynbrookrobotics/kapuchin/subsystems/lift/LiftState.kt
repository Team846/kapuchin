package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class LiftState(val rng: ClosedRange<Length>) {

    High(30.Inch..66.Inch),
    Low(3.Inch..30.Inch),
    Bottom(-1.Inch..3.Inch),
    Undetermined(-1.Inch..66.Inch);

    companion object {
        val states = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
        operator fun invoke() = Subsystems.instance?.let {
            it.lift?.hardware?.position?.optimizedRead(currentTime, 0.Second)?.y.let {
                if (it == null) {
                    LiftState.Undetermined
                } else {
                    when (it) {
                        in LiftState.High.rng -> LiftState.High
                        in LiftState.Low.rng -> LiftState.Low
                        in LiftState.Bottom.rng -> LiftState.Bottom
                        else -> LiftState.Undetermined
                    }
                }
            }
        }

        fun legalRanges(): List<ClosedRange<Length>> {
            val (legal, illegal) = Safeties.currentState(lift = LiftState().takeIf { it == Undetermined })
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

