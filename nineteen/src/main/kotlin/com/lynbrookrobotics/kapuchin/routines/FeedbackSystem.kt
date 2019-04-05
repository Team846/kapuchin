package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun FeedbackSystemComponent.set(feedback: Feedback) = startRoutine("Set") {
    controller { feedback }
}

suspend fun FeedbackSystemComponent.trackLineFeedback(lineScanner: LineScannerHardware, collectorSlider: CollectorSliderComponent) = startRoutine("Track line feedback") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps
    var feedback = Feedback(0.Percent, 0.Percent, 0.Percent, 0.Percent, Color.RED)

    controller {
        target?.let { snapshot ->
            if ((snapshot - current).abs < 1.5.Inch) {
                feedback = Feedback(100.Percent,100.Percent,100.Percent,100.Percent, Color.GREEN)
            }
        }

        feedback
    }
}