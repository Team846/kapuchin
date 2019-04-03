package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*

//suspend fun Subsystems.feedbackSystemTeleop() = startChoreo("Feedback system teleop") {
//
//    val lineTrackingButton by operator.lineTracking.readEagerly().withoutStamps
//
//    choreography {
//        runWhenever(
//                { lineTrackingButton } to choreography { collectorSlider?.let { lineTrackingAlignFeedback(it) } }
//        )
//    }
//}