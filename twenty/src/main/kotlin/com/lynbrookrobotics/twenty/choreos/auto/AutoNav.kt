package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Auto
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.followTrajectory
import info.kunalsheth.units.generated.*
import kotlin.system.measureTimeMillis

suspend fun Subsystems.autoNavBarrel() = startChoreo("AutoNav Barrel") {
    choreography { timePath(Auto.AutoNav.barrel) }
}

suspend fun Subsystems.autoNavSlalom() = startChoreo("AutoNav Slalom") {
    choreography { timePath(Auto.AutoNav.slalom) }
}

suspend fun Subsystems.autoNavBounce() = startChoreo("AutoNav Bounce") {
    choreography {
        val trajectories = (1..4)
            .map { i ->
                val fileName = "${Auto.AutoNav.bounce.name}$i"
                val path = loadRobotPath(fileName) ?: run {
                    log(Error) { "Couldn't find path $fileName" }
                    return@choreography
                }

                fastAsFuckPath(path, Auto.AutoNav.bounce)
            }

        val time = measureTimeMillis {
            trajectories.forEach {
                drivetrain.followTrajectory(it, Auto.AutoNav.bounce)
            }
        }

        log(Debug) { "bounce finished: ${time.Second withDecimals 3}s" }
    }
}