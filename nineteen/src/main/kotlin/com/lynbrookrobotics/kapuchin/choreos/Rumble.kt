package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*

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