package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.Safeties.LiftState
import com.lynbrookrobotics.kapuchin.Safeties.HandoffPivotState
import com.lynbrookrobotics.kapuchin.Safeties.CollectorSliderState
import com.lynbrookrobotics.kapuchin.Safeties.liftStates
import com.lynbrookrobotics.kapuchin.Subsystems.collectorPivot
import com.lynbrookrobotics.kapuchin.Subsystems.collectorSlider
import com.lynbrookrobotics.kapuchin.Subsystems.handoffPivot
import com.lynbrookrobotics.kapuchin.Subsystems.hookSlider
import com.lynbrookrobotics.kapuchin.Subsystems.lift
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

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

    sealed class LiftState(val rng: ClosedRange<Length>, val code: Int) {
        object High : LiftState(30.Inch..80.Inch, 0b00_00_000_0_0)
        object Low : LiftState(3.Inch..16.Inch, 0b01_00_000_0_0)
        object Bottom : LiftState(-1.Inch..3.Inch, 0b10_00_000_0_0)

        companion object {
            val liftQueryCode = 0b11_000_00_0_0
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

    val liftStates = arrayOf(LiftState.High, LiftState.Low, LiftState.Bottom)
    private fun LiftComponent.decode(state: RobotState): LiftState? {
      val liftCode = state.code and LiftState.liftQueryCode
      return liftStates.find {it.code == liftCode }
    }
    fun LiftComponent.legalRanges() = currentState(lift = null)
            .filter { it !in illegalStates }
            .mapNotNull { decode(it)?.rng }

    sealed class HandoffPivotState(val rng: ClosedRange<Angle>, val code: Int) {
        object High : HandoffPivotState(30.Degree..0.Degree, 0b00_00_000_0_0)
        object Mid : HandoffPivotState(60.Degree..30.Degree, 0b00_01_000_0_0)
        object Low : HandoffPivotState(90.Degree..60.Degree, 0b00_10_000_0_0)

        companion object {
          val handoffPivotQueryCode = 0b00_11_000_0_0
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

    val handoffPivotStates = arrayOf(HandoffPivotState.High, HandoffPivotState.Mid, HandoffPivotState.Low)
    private fun HandoffPivotComponent.decode(state: RobotState): HandoffPivotState? {
      val handoffCode = state.code and HandoffPivotState.handoffPivotQueryCode
      return handoffPivotStates.find {it.code == handoffCode }
    }
    fun HandoffPivotComponent.legalRanges() = currentState(handoffPivot = null)
            .filter { it !in illegalStates }
            .mapNotNull { decode(it)?.rng }

    sealed class CollectorSliderState(val rng: ClosedRange<Length>, val code: Int) {
        object WideLeft : CollectorSliderState(-16.Inch..-3.Inch, 0b00_00_000_0_0)
        object NarrowLeft : CollectorSliderState(-3.Inch..-1.Inch, 0b00_00_001_0_0)
        object Center : CollectorSliderState(-1.Inch..1.Inch, 0b00_00_100_0_0)
        object NarrowRight : CollectorSliderState(1.Inch..3.Inch, 0b00_00_110_0_0)
        object WideRight : CollectorSliderState(3.Inch..16.Inch, 0b00_01_000_0_0)

        companion object {
          val collectorSliderQueryCode = 0b00_00_111_0_0
            operator fun invoke() = collectorSlider.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in CollectorSliderState.WideLeft.rng -> CollectorSliderState.WideLeft
                    in CollectorSliderState.WideRight.rng -> CollectorSliderState.WideRight
                    in CollectorSliderState.NarrowLeft.rng -> CollectorSliderState.NarrowLeft
                    in CollectorSliderState.NarrowRight.rng -> CollectorSliderState.NarrowRight
                    in CollectorSliderState.Center.rng -> CollectorSliderState.Center
                    else -> null
                }
            }
        }
    }

    val collectorSliderStates = arrayOf(CollectorSliderState.WideLeft,
                                        CollectorSliderState.WideRight,
                                        CollectorSliderState.NarrowLeft,
                                        CollectorSliderState.NarrowRight,
                                        CollectorSliderState.Center)

    private fun CollectorSliderComponent.decode(state: RobotState): CollectorSliderState? {
      val collectorSliderCode = state.code and CollectorSliderState.collectorSliderQueryCode
      return collectorSliderStates.find {it.code == collectorSliderCode }
    }
    fun CollectorSliderComponent.legalRanges() = currentState(collectorSlider = null)
            .filter { it !in illegalStates }
            .mapNotNull { decode(it)?.rng }

    val collectorPivotStates = arrayOf(CollectorPivotPosition.Up, CollectorPivotPosition.Down)
    fun CollectorPivotState() = collectorPivot.hardware.solenoid.get().let {
        when (it) {
            CollectorPivotPosition.Up.output -> CollectorPivotPosition.Up
            CollectorPivotPosition.Down.output -> CollectorPivotPosition.Down
            else -> null
        }
    }

    private fun CollectorPivotComponent.decode(state: RobotState): CollectorPivotPosition? {
        val collectorSliderCode = state.code and CollectorPivotPosition.collectorPivotQueryCode
        return when (collectorSliderCode) {
          0b00_000_00_1_0 -> CollectorPivotPosition.Up
          0b00_00_000_0_0 -> CollectorPivotPosition.Down
          else -> null
        }
    }

    fun CollectorPivotComponent.legalRanges() = currentState(collectorPivot = null)
            .filter { it !in illegalStates }
            .mapNotNull { decode(it) }



    val hookSliderStates = arrayOf(HookSliderPosition.In, HookSliderPosition.Out)
    fun HookSliderState() = hookSlider.hardware.solenoid.get().let {
        when (it) {
            HookSliderPosition.In.output -> HookSliderPosition.In
            HookSliderPosition.Out.output -> HookSliderPosition.Out
            else -> null
        }
    }

    private fun HookSliderComponent.decode(state: RobotState): HookSliderPosition? {
        val hookSliderCode = state.code and HookSliderPosition.hookSliderQueryCode
        return when(hookSliderCode) {
          0b00_00_000_0_1 -> HookSliderPosition.In
          0b00_00_000_0_0 -> HookSliderPosition.Out
          else -> null
        }
    }

    fun HookSliderComponent.legalRanges() = currentState(hookSlider = null)
            .filter { it !in illegalStates }
            .mapNotNull { decode(it) }
}
