package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import info.kunalsheth.units.generated.*

suspend fun RumbleComponent.set(rumble: Rumble) = startRoutine("Set") {
    controller { rumble }
}

suspend fun RumbleComponent.trackLineFeedback(lineScanner: LineScannerHardware, collectorSlider: CollectorSliderComponent) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps

    controller {
        target?.takeIf { (it - current).abs < 1.5.Inch }?.let {
            TwoSided(0.Percent, 100.Percent)
        } ?: TwoSided(0.Percent)
    }
}
