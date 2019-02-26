package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

enum class LiftState(val rng: ClosedRange<Length>) {

    High(30.Inch..80.Inch),
    Low(3.Inch..16.Inch),
    Bottom(-1.Inch..3.Inch);

    companion object {
        val pos = 1
        val states = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
        operator fun invoke() = Subsystems.lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in LiftState.Bottom.rng -> LiftState.Bottom.also { println("LiftState: Bottom") }
                in LiftState.Low.rng -> LiftState.Low.also { println("LiftState: Low") }
                in LiftState.High.rng -> LiftState.High.also { println("LiftState: High") }
                else -> null.also { println("LiftState: Unknown") }
            }
        }

        fun legalRanges() = if (Subsystems.finishedInitialization) {
            Safeties.currentState(lift = null)
                    .filter { it !in Safeties.illegalStates }
                    .mapNotNull { decode(it)?.rng }
        } else {
            sequence { }
        }
    }
}
