package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.Subsystems.collectorPivot
import com.lynbrookrobotics.kapuchin.Subsystems.collectorSlider
import com.lynbrookrobotics.kapuchin.Subsystems.handoffPivot
import com.lynbrookrobotics.kapuchin.Subsystems.hookSlider
import com.lynbrookrobotics.kapuchin.Subsystems.lift
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

object Safeties {
    enum class LiftState(val rng: ClosedRange<Length>) {
        High(16.Inch..80.Inch),
        Low(-1.Inch..16.Inch);

        companion object {
            operator fun invoke() = lift.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in LiftState.Low.rng -> LiftState.Low
                    in LiftState.High.rng -> LiftState.High
                    else -> null
                }
            }
        }
    }

    enum class HandoffPivotState(val rng: ClosedRange<Angle>) {
        High(30.Degree..0.Degree),
        Mid(60.Degree..30.Degree),
        Low(90.Degree..60.Degree);

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

    enum class CollectorSliderState(vararg val rng: ClosedRange<Length>) {
        Wide(-16.Inch..-3.Inch, 3.Inch..16.Inch),
        Narrow(-3.Inch..3.Inch);

        companion object {
            operator fun invoke() = collectorSlider.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
                when (it) {
                    in CollectorSliderState.Wide.rng[0] -> CollectorSliderState.Wide
                    in CollectorSliderState.Wide.rng[1] -> CollectorSliderState.Wide
                    in CollectorSliderState.Narrow.rng[0] -> CollectorSliderState.Narrow
                    else -> null
                }
            }
        }
    }

    val collectorPivotState
        get() = collectorPivot.hardware.solenoid.get().let {
            when (it) {
                CollectorPivotPosition.Up.output -> CollectorPivotPosition.Up
                CollectorPivotPosition.Down.output -> CollectorPivotPosition.Down
                else -> null
            }
        }

    val hookSliderState
        get() = hookSlider.hardware.solenoid.get().let {
            when (it) {
                HookSliderPosition.In.output -> HookSliderPosition.In
                HookSliderPosition.Out.output -> HookSliderPosition.Out
                else -> null
            }
        }
}