package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun FeedbackSystemComponent.set(feedback: Feedback) = startRoutine("Set") {
    controller { feedback }
}

suspend fun FeedbackSystemComponent.trackLineFeedback(lineScanner: LineScannerHardware, collectorSlider: CollectorSliderComponent) = startRoutine("Track line feedback") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps
    var feedback = Feedback.red

    controller {
        target?.let { snapshot ->
            if ((snapshot - current).abs < 1.5.Inch) {
                feedback = Feedback.green.fullRumble()
            }
        }

        feedback
    }
}

suspend fun FeedbackSystemComponent.rainbow() = startRoutine("Rainbow") {

    controller {
        Color(Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 1f))
    }
}
