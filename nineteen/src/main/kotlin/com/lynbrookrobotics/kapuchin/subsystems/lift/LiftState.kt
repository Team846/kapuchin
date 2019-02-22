package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

sealed class LiftState(val rng: ClosedRange<Length>, val code: Int) {
    object High : LiftState(30.Inch..80.Inch, 0b00_00_000_0_0)
    object Low : LiftState(3.Inch..16.Inch, 0b01_00_000_0_0)
    object Bottom : LiftState(-1.Inch..3.Inch, 0b10_00_000_0_0)

    companion object {
        val liftQueryCode = 0b11_000_00_0_0
        operator fun invoke() = Subsystems.lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in LiftState.Bottom.rng -> LiftState.Bottom
                in LiftState.Low.rng -> LiftState.Low
                in LiftState.High.rng -> LiftState.High
                else -> null
            }
        }
    }
}

val liftStates = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
private fun LiftComponent.decode(state: RobotState): LiftState? {
    val liftCode = state.code and LiftState.liftQueryCode
    return liftStates.find {it.code == liftCode }
}
fun LiftComponent.legalRanges() = Safeties.currentState(lift = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }