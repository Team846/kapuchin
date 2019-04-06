package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val visionAlign by driver.lineTracking.readEagerly().withoutStamps

    choreography {
        whenever({drivetrain.routine == null}) {
            runWhile({ !visionAlign }) {
                drivetrain.teleop(driver)
            }
            runWhile({ visionAlign }) {
                if (limelight != null) {
                    drivetrain.visionSnapshotTracking(5.FootPerSecond, limelight)
                }
                freeze()
            }
        }
    }
}