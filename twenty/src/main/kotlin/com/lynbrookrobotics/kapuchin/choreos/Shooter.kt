package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {
    choreography {

    }
}

suspend fun Subsystems.aim() = if (flywheel != null && limelight != null && shooterHood != null) startChoreo("Aim") {

    val target by limelight.hardware.readings.readEagerly().withoutStamps

    choreography {
        val snapshot1 = limelight.goalPositions(target ?: run {
            println("Target snapshot 1 not found")
            return@choreography
        })
        
        // TODO rotate turret with snapshot 1

        val snapshot2 = limelight.goalPositions(target ?: run {
            println("Target snapshot 2 not found")
            return@choreography
        })

        val (flywheelVelocity, _, shooterHoodState) = bestShot(snapshot2) ?: run {
            println("No shots possible")
            return@choreography
        }
    }

} else println("Couldn't run aim choreo because of null subsystems")