package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.CollectorSliderState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.CollectorSliderState.Companion.states
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlin.math.pow

sealed class CollectorSliderState(val rng: ClosedRange<Length>) {
    object WideLeft : CollectorSliderState(-16.Inch..-3.Inch)
    object NarrowLeft : CollectorSliderState(-3.Inch..-1.Inch)
    object Center : CollectorSliderState(-1.Inch..1.Inch)
    object NarrowRight : CollectorSliderState(1.Inch..3.Inch)
    object WideRight : CollectorSliderState(3.Inch..16.Inch)

    companion object {
        val pos = 3
        val states = arrayOf(CollectorSliderState.WideLeft,
                CollectorSliderState.WideRight,
                CollectorSliderState.NarrowLeft,
                CollectorSliderState.NarrowRight,
                CollectorSliderState.Center)
        operator fun invoke() = Subsystems.collectorSlider.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
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

fun CollectorSliderComponent.legalRanges() = Safeties.currentState(collectorSlider = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }
