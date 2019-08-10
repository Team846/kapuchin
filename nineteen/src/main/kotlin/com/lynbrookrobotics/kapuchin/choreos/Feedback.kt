package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.choreographies.*
import com.lynbrookrobotics.kapuchin.routines.*

suspend fun Subsystems.feedbackTeleop() = collectorSlider?.let { slider ->
    startChoreo("Feedback teleop") {
        choreography {
            launchWhenever(
                    { rumble.routine == null } to choreography {
                        rumble.trackLineFeedback(lineScanner, slider)
                    }
            )
            freeze()
        }
    }
}