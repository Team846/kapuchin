package com.lynbrookrobotics.nineteen.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.nineteen.subsystems.collector.slider.CollectorSliderComponent
import com.lynbrookrobotics.nineteen.subsystems.driver.Rumble
import com.lynbrookrobotics.nineteen.subsystems.driver.RumbleComponent
import com.lynbrookrobotics.nineteen.subsystems.drivetrain.LineScannerHardware
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
