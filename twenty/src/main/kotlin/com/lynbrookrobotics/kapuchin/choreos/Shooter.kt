package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {
    choreography {

    }
}

// TODO aim (shooter-control branch)
// TODO shoot (shooter-control branch)