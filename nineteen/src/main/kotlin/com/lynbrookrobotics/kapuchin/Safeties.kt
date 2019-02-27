package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
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

        //9 always represents `Undetermined`

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
            return (if (index >= 0) index else 9) * 10.0.pow(LiftState.pos - 1).toInt()
        }

        fun HandoffPivotState.encode(): Int {
            val index = HandoffPivotState.states.indexOf(this)
            return (if (index >= 0) index else 9) * 10.0.pow(HandoffPivotState.pos - 1).toInt()
        }

        fun CollectorSliderState.encode(): Int {
            val index = CollectorSliderState.states.indexOf(this)
            return (if (index >= 0) index else 9) * 10.0.pow(CollectorSliderState.pos - 1).toInt()
        }

        fun CollectorPivotState.encode(): Int {
            val index = CollectorPivotState.states.indexOf(this)
            return (if (index >= 0) index else 9) * 10.0.pow(CollectorPivotState.pos - 1).toInt()
        }

        fun HookSliderState.encode(): Int {
            val index = HookSliderState.states.indexOf(this)
            return (if (index >= 0) index else 9) * 10.0.pow(HookSliderState.pos - 1).toInt()
        }

        fun LiftState.Companion.decode(state: RobotState): LiftState? {
            val index = state.code / (10.0.pow(LiftState.pos - 1).toInt()) % 10
            return LiftState.Undetermined.takeIf { it -> index == 9 } ?: LiftState.states[index]
        }

        fun HandoffPivotState.Companion.decode(state: RobotState): HandoffPivotState? {
            val index = state.code / (10.0.pow(HandoffPivotState.pos -1).toInt()) % 10
            return HandoffPivotState.Undetermined.takeIf { it -> index == 9 } ?: HandoffPivotState.states[index]
        }

        fun CollectorSliderState.Companion.decode(state: RobotState): CollectorSliderState? {
            val index = state.code / (10.0.pow(CollectorSliderState.pos - 1).toInt()) % 10
            return CollectorSliderState.Undetermined.takeIf { it -> index == 9 } ?:CollectorSliderState.states[index]
        }

        fun CollectorPivotState.Companion.decode(state: RobotState): CollectorPivotState? {
            val index = state.code / (10.0.pow(CollectorPivotState.pos - 1).toInt()) % 10
            return CollectorPivotState.Undetermined.takeIf { it -> index == 9 } ?:CollectorPivotState.states[index]
        }

        fun HookSliderState.Companion.decode(state: RobotState): HookSliderState? {
            val index = state.code / (10.0.pow(CollectorPivotState.pos - 1).toInt()) % 10
            return HookSliderState.Undetermined.takeIf { it -> index == 9 } ?: HookSliderState.states[index]
        }
    }
}

object Safeties : Named by Named("safeties") {

    val log by pref(true)

    fun init() {
//        LiftState.init()
//        HandoffPivotState.init()
//        CollectorSliderState.init()
//        CollectorPivotState.init()
//        HookSliderState.init()
        initIllegalStates()
    }


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

    lateinit var illegalStates: List<RobotState>

    fun currentState(
            lift: LiftState? = LiftState(),
            handoffPivot: HandoffPivotState? = HandoffPivotState(),
            collectorSlider: CollectorSliderState? = CollectorSliderState(),
            collectorPivot: CollectorPivotState? = CollectorPivotState(),
            hookSlider: HookSliderState? = HookSliderState()
    ) = permuteState(lift, handoffPivot, collectorSlider, collectorPivot, hookSlider)

     private fun initIllegalStates() {
        illegalStates = setOf(
                permuteState(lift = LiftState.Low, collectorPivot = CollectorPivotState.Down, hookSlider = HookSliderState.Out),
                permuteState(lift = LiftState.Low, collectorSlider = CollectorSliderState.WideLeft),
                permuteState(lift = LiftState.Low, collectorSlider = CollectorSliderState.WideRight),
                permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.NarrowLeft),
                permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.NarrowRight),
                permuteState(lift = LiftState.Low, collectorSlider = CollectorSliderState.NarrowLeft, collectorPivot = CollectorPivotState.Down),
                permuteState(lift = LiftState.Low, collectorSlider = CollectorSliderState.NarrowRight, collectorPivot = CollectorPivotState.Down)
                        //permuteState(lift = LiftState.Bottom, handoffPivot = HandoffPivotState.Vertical)
        ).flatMap { it.asIterable() }
    }
}
