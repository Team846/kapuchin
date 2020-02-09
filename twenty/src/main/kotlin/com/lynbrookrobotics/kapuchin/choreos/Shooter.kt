package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

suspend fun Subsystems.shooterTeleop() = startChoreo("Shooter Teleop") {
    choreography {

    }
}

// TODO aim (shooter-control branch)
// TODO shoot (shooter-control branch)