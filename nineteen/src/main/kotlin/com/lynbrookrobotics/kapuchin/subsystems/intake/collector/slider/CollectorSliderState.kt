package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

sealed class CollectorSliderState(val rng: ClosedRange<Length>, val code: Int) {
    object WideLeft : CollectorSliderState(-16.Inch..-3.Inch, 0b00_00_000_0_0)
    object NarrowLeft : CollectorSliderState(-3.Inch..-1.Inch, 0b00_00_001_0_0)
    object Center : CollectorSliderState(-1.Inch..1.Inch, 0b00_00_100_0_0)
    object NarrowRight : CollectorSliderState(1.Inch..3.Inch, 0b00_00_110_0_0)
    object WideRight : CollectorSliderState(3.Inch..16.Inch, 0b00_01_000_0_0)

    companion object {
        val collectorSliderQueryCode = 0b00_00_111_0_0
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

val collectorSliderStates = arrayOf(CollectorSliderState.WideLeft,
        CollectorSliderState.WideRight,
        CollectorSliderState.NarrowLeft,
        CollectorSliderState.NarrowRight,
        CollectorSliderState.Center)

private fun CollectorSliderComponent.decode(state: RobotState): CollectorSliderState? {
    val collectorSliderCode = state.code and CollectorSliderState.collectorSliderQueryCode
    return collectorSliderStates.find {it.code == collectorSliderCode }
}
fun CollectorSliderComponent.legalRanges() = Safeties.currentState(collectorSlider = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it)?.rng }