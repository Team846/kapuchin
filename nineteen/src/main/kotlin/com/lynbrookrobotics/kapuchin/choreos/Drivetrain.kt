package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val visionAlign by driver.lineTracking.readEagerly().withoutStamps

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
            runWhenever(
                { drivetrain.routine == null && !visionAlign } to choreography {
                    drivetrain.teleop(driver)
                },
                { drivetrain.routine == null && visionAlign } to choreography {
                    if (limelight != null && collectorSlider != null) limeLineAlign(
                            drivetrain, limelight, lineScanner, collectorSlider, electrical
                    ) else freeze()
                }
            )
        } catch (t: Throwable) {
            log(Error, t) { "The drivetrain teleop control is exiting!!!" }
            throw t
        }
    }
}