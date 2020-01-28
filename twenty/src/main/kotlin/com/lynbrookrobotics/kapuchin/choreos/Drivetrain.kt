package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

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

                )
            }
            freeze()
        } catch (t: Throwable) {
            log(Error, t) { "The drivetrain teleop control is exiting!!!" }
            throw t
        }
    }
}