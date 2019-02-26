package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.CollectorSliderState.Companion.pos
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.CollectorSliderState.Companion.states
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlin.math.pow

enum class CollectorSliderState(val rng: ClosedRange<Length>) {

    WideLeft(-16.Inch..-3.Inch),
    NarrowLeft(-3.Inch..-1.Inch),
    Center(-1.Inch..1.Inch),
    NarrowRight(1.Inch..3.Inch),
    WideRight(3.Inch..16.Inch);

    companion object {
        val pos = 3
        val states = arrayOf(CollectorSliderState.WideLeft,
                CollectorSliderState.WideRight,
                CollectorSliderState.NarrowLeft,
                CollectorSliderState.NarrowRight,
                CollectorSliderState.Center)
        operator fun invoke() = Subsystems.collectorSlider.hardware.position.optimizedRead(currentTime, 0.Second).y.let {
            when (it) {
                in CollectorSliderState.WideLeft.rng -> CollectorSliderState.WideLeft.also { println("CollectorSliderState: WideLeft") }
                in CollectorSliderState.WideRight.rng -> CollectorSliderState.WideRight.also { println("CollectorSliderState: WideRight") }
                in CollectorSliderState.NarrowLeft.rng -> CollectorSliderState.NarrowLeft.also { println("CollectorSliderState: NarrowLeft") }
                in CollectorSliderState.NarrowRight.rng -> CollectorSliderState.NarrowRight.also { println("CollectorSliderState: NarrowRight") }
                in CollectorSliderState.Center.rng -> CollectorSliderState.Center.also { println("CollectorSliderState: Center") }
                else -> null.also { println("CollectorSliderState: Unknown") }
            }
        }

        fun legalRanges() = if (Subsystems.finishedInitialization) {
            Safeties.currentState(collectorSlider = null)
                    .filter { it !in Safeties.illegalStates }
                    .mapNotNull { decode(it)?.rng }
        } else {
            sequence { }
        }
    }
}
