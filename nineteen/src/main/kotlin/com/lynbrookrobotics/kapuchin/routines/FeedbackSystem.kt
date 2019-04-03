package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.generated.*

suspend fun FeedbackSystemComponent.set(feedback: Feedback) = startRoutine("Set") {
    controller { feedback }
}

suspend fun FeedbackSystemComponent.trackLineFeedback(lineScanner: LineScannerHardware, collectorSlider: CollectorSliderComponent) = startRoutine("Track line feedback") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps
    var feedback = Feedback.RED

    controller {
        target?.let { snapshot ->
            if ((snapshot - current).abs < 1.5.Inch) {
                feedback = Feedback.GREEN.fullRumble
            }
        }

        feedback
    }
}