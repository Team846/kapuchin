package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.feedback.*
import info.kunalsheth.units.generated.*

suspend fun Rumble.set(rumble: TwoSided<DutyCycle>) = startRoutine("Set") {
    controller { rumble }
}

suspend fun Rumble.trackLineFeedback(lineScanner: LineScanner, collectorSlider: CollectorSlider) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps

    controller {
        target?.takeIf { (it - current).abs < 1.5.Inch }?.let {
            TwoSided(0.Percent, 100.Percent)
        } ?: TwoSided(0.Percent)
    }
}
