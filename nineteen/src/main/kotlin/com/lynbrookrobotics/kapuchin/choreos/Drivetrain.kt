package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val autoAlign by driver.autoAlign.readEagerly().withoutStamps

    choreography {
        try {
            launch {
                launchWhenever(
                        { drivetrain.routine == null } to choreography {
                            drivetrain.teleop(driver)
                        }
                )
            }
            launch {
                runWhenever(
                        { autoAlign } to choreography {
                            launch { collectorSlider?.trackLine(lineScanner, electrical) }
                            drivetrain.lineActiveTracking(
                                    2.FootPerSecond,
                                    collectorSlider
                                            ?.run { (min-1.Inch)..(max+1.Inch) }
                                            ?: -5.Inch..5.Inch,
                                    lineScanner
                            )
                        }
                )
            }
            freeze()
        } catch (t: Throwable) {
            log(Error, t) { "The drivetrain teleop control is exiting!!!" }
            throw t
        }
    }
}