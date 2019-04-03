package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import info.kunalsheth.units.generated.*

suspend fun Subsystems.feedbackSystemTeleop() = startChoreo("Feedback system teleop") {

    val lineTrackingButton by operator.lineTracking.readEagerly().withoutStamps
    val target by lineScanner.linePosition.readEagerly().withoutStamps
    val current by collectorSlider?.hardware!!.position.readEagerly().withoutStamps

    val xButton by operator.cargoShipCargoHeight.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { lineTrackingButton && !lineTrackingAligned(target, current) }
                        to choreography { feedbackSystem.set(Feedback.RED) },
                { lineTrackingButton && lineTrackingAligned(target, current) }
                        to choreography { feedbackSystem.set(Feedback.GREEN.fullRumble) },
                { xButton }
                        to choreography { feedbackSystem.set(Feedback.BLUE.fullRumble) }
        )
    }

}

private fun lineTrackingAligned(target: Length?, current: Length): Boolean {
    target?.let { snapshot ->
        if ((snapshot - current).abs < 1.5.Inch) return true
    }
    return false
}