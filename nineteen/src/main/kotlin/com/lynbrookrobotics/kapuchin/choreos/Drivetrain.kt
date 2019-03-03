package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.isActive

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val visionAlign by driver.visionAlign.readEagerly().withoutStamps

    choreography {
        while (isActive) {
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