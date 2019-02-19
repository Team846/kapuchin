package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.Subsystems.collectorPivot
import com.lynbrookrobotics.kapuchin.Subsystems.collectorSlider
import com.lynbrookrobotics.kapuchin.Subsystems.handoffPivot
import com.lynbrookrobotics.kapuchin.Subsystems.hookSlider
import com.lynbrookrobotics.kapuchin.Subsystems.lift
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

object Safeties : Named by Named("safeties") {

    val log by pref(true)

    data class RobotState(val lift: LiftState,
                          val handoffPivot: HandoffPivotState,
                          val collectorSlider: CollectorSliderState,
                          val collectorPivot: CollectorPivotPosition,
                          val hookSlider: HookSliderPosition)

    fun permuteState(
            lift: LiftState? = null,
            handoffPivot: HandoffPivotState? = null,
            collectorSlider: CollectorSliderState? = null,
            collectorPivot: CollectorPivotPosition? = null,
            hookSlider: HookSliderPosition? = null
    ): Set<RobotState> {

        val ls = if (lift == null) liftStates else setOf(lift)
        val hps = if (handoffPivot == null) handoffPivotStates else setOf(handoffPivot)
        val css = if (collectorSlider == null) collectorSliderStates else setOf(collectorSlider)
        val cps = if (collectorPivot == null) collectorPivotStates else setOf(collectorPivot)
        val hss = if (hookSlider == null) hookSliderStates else setOf(hookSlider)

        val out = mutableSetOf<RobotState>()

        for (l in ls) {
            for (hp in hps) {
                for (cs in css) {
                    for (cp in cps) {
                        for (hs in hss) {
                            out.add(RobotState(l, hp, cs, cp, hs))
                        }
                    }
                }
            }
        }

        return out
    }

    fun currentState(
            lift: LiftState? = LiftState(),
            handoffPivot: HandoffPivotState? = HandoffPivotState(),
            collectorSlider: CollectorSliderState? = CollectorSliderState(),
            collectorPivot: CollectorPivotPosition? = CollectorPivotState(),
            hookSlider: HookSliderPosition? = HookSliderState()
    ): Set<RobotState> = permuteState(lift, handoffPivot, collectorSlider, collectorPivot, hookSlider)


    val illegalStates = setOf(
            permuteState(lift = LiftState.Low, handoffPivot = null, collectorSlider = null, collectorPivot = CollectorPivotPosition.Down, hookSlider = HookSliderPosition.Out),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.High, collectorSlider = CollectorSliderState.Wide, collectorPivot = null, hookSlider = null),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Low, collectorSlider = CollectorSliderState.Wide, collectorPivot = null, hookSlider = null),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.Wide, collectorPivot = null, hookSlider = null),
            permuteState(lift = LiftState.Low, handoffPivot = HandoffPivotState.Mid, collectorSlider = CollectorSliderState.Narrow, collectorPivot = null, hookSlider = null),
            permuteState(lift = LiftState.Bottom, handoffPivot = null, collectorSlider = CollectorSliderState.Wide, collectorPivot = null, hookSlider = null)
    ).flatten()

    sealed class LiftState(val rng: ClosedRange<Length>) {
        object High : LiftState(30.Inch..80.Inch)
        object Low : LiftState(3.Inch..16.Inch)
        object Bottom : LiftState(-1.Inch..3.Inch)

        companion object {
            operator fun invoke() = lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in LiftState.Bottom.rng -> LiftState.Bottom
                    in LiftState.Low.rng -> LiftState.Low
                    in LiftState.High.rng -> LiftState.High
                    else -> null
                }
            }
        }
    }

    val liftStates = setOf(LiftState.High, LiftState.Low, LiftState.Bottom)
    fun LiftComponent.legalRanges() = currentState(lift = null).asSequence()
            .filter { curr ->
                illegalStates.none { ill ->
                    curr.equals(ill)
                }
            }
            .map { it.lift }
            .map { it.rng }.toSet()

    sealed class HandoffPivotState(val rng: ClosedRange<Angle>) {
        object High : HandoffPivotState(30.Degree..0.Degree)
        object Mid : HandoffPivotState(60.Degree..30.Degree)
        object Low : HandoffPivotState(90.Degree..60.Degree)

        companion object {
            operator fun invoke() = handoffPivot.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in HandoffPivotState.High.rng -> HandoffPivotState.High
                    in HandoffPivotState.Mid.rng -> HandoffPivotState.Mid
                    in HandoffPivotState.Low.rng -> HandoffPivotState.Low
                    else -> null
                }
            }
        }
    }

    val handoffPivotStates = setOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
    fun HandoffPivotComponent.legalRanges() = currentState(handoffPivot = null).asSequence()
            .filter { curr ->
                illegalStates.none { ill ->
                    curr == ill
                }
            }
            .map { it.handoffPivot }
            .map { it.rng }.toSet()

    sealed class CollectorSliderState(vararg val rng: ClosedRange<Length>) {
        object Wide : CollectorSliderState(-16.Inch..-3.Inch, 3.Inch..16.Inch)
        object Narrow : CollectorSliderState(-3.Inch..-1.Inch, 1.Inch..3.Inch)
        object Center : CollectorSliderState(-1.Inch..1.Inch)

        companion object {
            operator fun invoke() = collectorSlider.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in CollectorSliderState.Wide.rng[0],
                    in CollectorSliderState.Wide.rng[1] -> CollectorSliderState.Wide
                    in CollectorSliderState.Narrow.rng[0],
                    in CollectorSliderState.Narrow.rng[1] -> CollectorSliderState.Narrow
                    in CollectorSliderState.Center.rng[0] -> CollectorSliderState.Center
                    else -> null
                }
            }
        }
    }

    val collectorSliderStates = setOf(CollectorSliderState.Wide, CollectorSliderState.Narrow, CollectorSliderState.Center)
    fun CollectorSliderComponent.legalRanges() = currentState(collectorSlider = null).asSequence()
            .filter { curr ->
                illegalStates.none { ill ->
                    curr.equals(ill)
                }
            }
            .map { it.collectorSlider }
            .map { it.rng }.toSet()

    val collectorPivotStates = setOf(CollectorPivotPosition.Up, CollectorPivotPosition.Down)
    fun CollectorPivotState() = collectorPivot.hardware.solenoid.get().let {
        when (it) {
            CollectorPivotPosition.Up.output -> CollectorPivotPosition.Up
            CollectorPivotPosition.Down.output -> CollectorPivotPosition.Down
            else -> null
        }
    }

    fun CollectorPivotComponent.legalRanges() = currentState(handoffPivot = null).asSequence()
            .filter { curr ->
                illegalStates.none { ill ->
                    curr.equals(ill)
                }
            }
            .map { it.collectorPivot }.toSet()

    val hookSliderStates = setOf(HookSliderPosition.In, HookSliderPosition.Out)
    fun HookSliderState() = hookSlider.hardware.solenoid.get().let {
        when (it) {
            HookSliderPosition.In.output -> HookSliderPosition.In
            HookSliderPosition.Out.output -> HookSliderPosition.Out
            else -> null
        }
    }

    fun HookSliderComponent.legalRanges() = currentState(lift = null).asSequence()
            .filter { curr ->
                illegalStates.none { ill ->
                    curr.equals(ill)
                }
            }
            .map { it.hookSlider }.toSet()
}
