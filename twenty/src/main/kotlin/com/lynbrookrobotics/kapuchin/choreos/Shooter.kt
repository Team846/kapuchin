package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import kotlinx.coroutines.supervisorScope

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {

    choreography {
        runWhenever(

        )
    }
}
suspend fun Subsystems.shoot() = supervisorScope() {
    try {
        FeederRoller
    }
}