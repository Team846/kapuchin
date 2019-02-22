package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*

inline class RobotState(val code: Int) {


    //lower bit value is towards the right
    //8-9     6-7      3-5       2        1        10-32
    //lift    hPivot   cSlider   cPivot   hSlider  free bits

    companion object {
        operator fun invoke(lift: LiftState,
                            handoffPivot: HandoffPivotState,
                            collectorSlider: CollectorSliderState,
                            collectorPivot: CollectorPivotPosition,
                            hookSlider: HookSliderPosition): RobotState {
            var code: Int = 0b00_00_000_0_0
            code = lift.code or handoffPivot.code or collectorSlider.code
            if (collectorPivot == CollectorPivotPosition.Up) {
                code = code or 0b00_000_00_1_0
            }
            if (hookSlider == HookSliderPosition.Out) {
                code = code or 0b00_00_000_0_1
            }
            return RobotState(code)
        }
    }
}

object Safeties : Named by Named("safeties") {

    val log by pref(true)

    private fun permuteState(
            lift: LiftState? = null,
            handoffPivot: HandoffPivotState? = null,
            collectorSlider: CollectorSliderState? = null,
            collectorPivot: CollectorPivotPosition? = null,
            hookSlider: HookSliderPosition? = null
    ) = sequence {
        val ls = if (lift == null) liftStates else arrayOf(lift)
        val hps = if (handoffPivot == null) handoffPivotStates else arrayOf(handoffPivot)
        val css = if (collectorSlider == null) collectorSliderStates else arrayOf(collectorSlider)
        val cps = if (collectorPivot == null) collectorPivotStates else arrayOf(collectorPivot)
        val hss = if (hookSlider == null) hookSliderStates else arrayOf(hookSlider)

        for (l in ls) {
            for (hp in hps) {
                for (cs in css) {
                    for (cp in cps) {
                        for (hs in hss) {
                            yield(RobotState(l, hp, cs, cp, hs))
                        }
                    }
                }
            }
        }
    }

    val illegalStates = setOf(
            permuteState(lift = LiftState.Low, collectorPivot = CollectorPivotPosition.Down, hookSlider = HookSliderPosition.Out),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.High, collectorSlider = CollectorSliderState.WideLeft),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.High, collectorSlider = CollectorSliderState.WideRight),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Low, collectorSlider = CollectorSliderState.WideLeft),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Low, collectorSlider = CollectorSliderState.WideRight),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.WideLeft),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.WideRight),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.NarrowLeft),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.NarrowRight),
            permuteState(lift = LiftState.Bottom, collectorSlider = CollectorSliderState.WideLeft),
            permuteState(lift = LiftState.Bottom, collectorSlider = CollectorSliderState.WideRight)
    ).flatMap { it.asIterable() }

    fun currentState(
            lift: LiftState? = LiftState(),
            handoffPivot: HandoffPivotState? = HandoffPivotState(),
            collectorSlider: CollectorSliderState? = CollectorSliderState(),
            collectorPivot: CollectorPivotPosition? = CollectorPivotState(),
            hookSlider: HookSliderPosition? = HookSliderState()
    ) = permuteState(lift, handoffPivot, collectorSlider, collectorPivot, hookSlider)
}
