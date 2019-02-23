package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.HandoffPivotState.Companion
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.LiftState.*
import kotlin.math.pow

inline class RobotState(val code: Int) {

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
        fun LiftState.encode(): Int {
            val index = LiftState.states.indexOf(this)
            return if (index >= 0) index * 10.0.pow(LiftState.pos - 1).toInt() else throw Throwable("Unknown lift state encountered")
        }

        fun LiftComponent.decode(state: RobotState): LiftState? {
            val index = state.code / (10.0.pow(LiftState.pos).toInt()) % 10
            return LiftState.states[index]
        }
        fun HandoffPivotState.encode(): Int {
            val index = HandoffPivotState.states.indexOf(this)
            return if (index >= 0) index * 10.0.pow(HandoffPivotState.pos - 1).toInt() else throw Throwable("Unknown handoff pivotstate encountered")
        }

        fun HandoffPivotComponent.decode(state: RobotState): HandoffPivotState? {
            val index = state.code / (10.0.pow(HandoffPivotState.pos).toInt()) % 10
            return HandoffPivotState.states[index]
        }
        fun CollectorSliderState.encode(): Int {
            val index = CollectorSliderState.states.indexOf(this)
            return if (index >= 0) index * 10.0.pow(CollectorSliderState.pos - 1).toInt() else throw Throwable("Unknown collector slider state encountered")
        }

        fun CollectorSliderComponent.decode(state: RobotState): CollectorSliderState? {
            val index = state.code / (10.0.pow(CollectorSliderState.pos).toInt()) % 10
            return CollectorSliderState.states[index]
        }
        fun CollectorPivotState.encode(): Int {
            val index = CollectorPivotState.states.indexOf(this)
            return if (index >= 0) index * 10.0.pow(CollectorPivotState.pos - 1).toInt() else throw Throwable("Unknown collector pivot state encountered")
        }

        fun CollectorPivotComponent.decode(state: RobotState): CollectorPivotState? {
            val index = state.code / (10.0.pow(CollectorPivotState.pos).toInt()) % 10
            return CollectorPivotState.states[index]
        }
        fun HookSliderState.encode(): Int {
            val index = HookSliderState.states.indexOf(this)
            return if (index >= 0) index * 10.0.pow(HookSliderState.pos - 1).toInt() else throw Throwable("Unknown hook slider state encountered")
        }

        fun HookSliderComponent.decode(state: RobotState): HookSliderState? {
            val index = state.code / (10.0.pow(CollectorPivotState.pos).toInt()) % 10
            return HookSliderState.states[index]
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
