package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Auto
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.followTrajectory
import info.kunalsheth.units.generated.*
import kotlin.system.measureTimeMillis

suspend fun Subsystems.autoNavBarrel() = startChoreo("AutoNav Barrel") {
    choreography {
        val start = currentTime
        try {
            timePath(Auto.AutoNav.barrel)
        } finally {
            val time = currentTime - start
            println("Finish in ${time.Second}s")
        }
    }
}

suspend fun Subsystems.autoNavSlalom() = startChoreo("AutoNav Slalom") {
    choreography {
        val start = currentTime
        try {
            timePath(Auto.AutoNav.slalom)
        } finally {
            val time = currentTime - start
            println("Finish in ${time.Second}s")
        }
    }
}

suspend fun Subsystems.autoNavBounce() = startChoreo("AutoNav Bounce") {

    val robotPos by drivetrain.hardware.position.readEagerly().withoutStamps

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
            val pathThetas = listOf(-14.21.Degree, 90.Degree, 90.Degree, 90.Degree)
            val origin = robotPos.copy()

            var reverse = false
            var sum = Waypoint(0.Foot, 0.Foot)

            for (i in trajectories.indices) {
                val p = (RotationMatrix(origin.bearing) rz sum) + origin.vector
                val pTheta = (origin.bearing `coterminal +` (pathThetas[i] - pathThetas[0]))
                    .let { if (reverse) it `coterminal +` 180.Degree else it }

                val pos = Position(p.x, p.y, pTheta)
                drivetrain.log(Debug) {"Starting position #$i: $pos" }

                drivetrain.followTrajectory(trajectories[i], Auto.AutoNav.bounce.copy(reverse = reverse), origin = pos)

                sum += RotationMatrix(pathThetas[i] - pathThetas[0]) rz trajectories[i].last().y
                reverse = !reverse
            }
        }

        log(Debug) { "bounce finished: ${time.Second withDecimals 3}s" }
    }
}