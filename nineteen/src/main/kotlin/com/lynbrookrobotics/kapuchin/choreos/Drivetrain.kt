package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import kotlinx.coroutines.launch

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val autoAlign by driver.autoAlign.readEagerly().withoutStamps

    choreography {
        //        whenever({ drivetrain.routine == null }) {
//            runWhile({ !visionAlign }) {
//                drivetrain.teleop(driver)
//            }
//            runWhile({ visionAlign }) {
//                if (limelight != null && collectorSlider != null) {
//                    limeLineAlign(
//                            drivetrain, limelight, lineScanner, collectorSlider, electrical
//                    )
//                }
//            }
//        }

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
                            if (limelight != null && collectorSlider != null) limeLineAlign(
                                    drivetrain, limelight, lineScanner, collectorSlider, electrical
                            ) else freeze()
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