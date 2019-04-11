package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.generated.*

suspend fun RumbleComponent.set(rumble: Rumble) = startRoutine("Set") {
    controller { rumble }
}

suspend fun RumbleComponent.trackLineFeedback(lineScanner: LineScannerHardware, collectorSlider: CollectorSliderComponent) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps
    var operator: DutyCycle

    controller {
        operator = 0.Percent
        target?.let { snapshot ->
            if ((snapshot - current).abs < 1.5.Inch) {
                operator = 100.Percent
            }
        }

        0.Percent to operator
    }
}
