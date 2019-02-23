package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

sealed class LiftState(val rng: ClosedRange<Length>) {
    object High : LiftState(30.Inch..80.Inch)
    object Mid : LiftState(16.Inch..30.Inch)
    object Low : LiftState(3.Inch..16.Inch)
    object Bottom : LiftState(-1.Inch..3.Inch)

    companion object {
        val pos = 1
        val states = arrayOf(LiftState.High, LiftState.Mid, LiftState.Low, LiftState.Bottom)
        operator fun invoke() = Subsystems.lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in LiftState.Bottom.rng -> LiftState.Bottom
                in LiftState.Low.rng -> LiftState.Low
                in LiftState.High.rng -> LiftState.High
                else -> null
            }
        }

        fun legalRanges() = Safeties.currentState(lift = null)
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it)?.rng }
    }
}


