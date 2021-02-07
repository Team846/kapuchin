package com.lynbrookrobotics.nineteen.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.nineteen.Subsystems
import com.lynbrookrobotics.nineteen.routines.trackLineFeedback

suspend fun Subsystems.rumbleTeleop() = collectorSlider?.let { slider ->
    startChoreo("Rumble teleop") {
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