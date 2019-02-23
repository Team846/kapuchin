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
                            collectorPivot: CollectorPivotState,
                            hookSlider: HookSliderState): RobotState {
            return RobotState(lift.encode() +
                    handoffPivot.encode() +
                    collectorSlider.encode() +
                    collectorPivot.encode() +
                    hookSlider.encode())
        }
    }
}

object Safeties : Named by Named("safeties") {

    val log by pref(true)

    private fun permuteState(
            lift: LiftState? = null,
            handoffPivot: HandoffPivotState? = null,
            collectorSlider: CollectorSliderState? = null,
            collectorPivot: CollectorPivotState? = null,
            hookSlider: HookSliderState? = null
    ) = sequence {
        val ls = if (lift == null) LiftState.states else arrayOf(lift)
        val hps = if (handoffPivot == null) HandoffPivotState.states else arrayOf(handoffPivot)
        val css = if (collectorSlider == null) CollectorSliderState.states else arrayOf(collectorSlider)
        val cps = if (collectorPivot == null) CollectorPivotState.states else arrayOf(collectorPivot)
        val hss = if (hookSlider == null) HookSliderState.states else arrayOf(hookSlider)

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
            permuteState(lift = LiftState.Low, collectorPivot = CollectorPivotState.Down, hookSlider = HookSliderState.Out),
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
            collectorPivot: CollectorPivotState? = CollectorPivotState(),
            hookSlider: HookSliderState? = HookSliderState()
    ) = permuteState(lift, handoffPivot, collectorSlider, collectorPivot, hookSlider)
}
