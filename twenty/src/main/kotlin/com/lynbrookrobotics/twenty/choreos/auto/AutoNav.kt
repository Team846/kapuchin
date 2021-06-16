package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Auto
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.followTrajectory
import info.kunalsheth.units.generated.*

suspend fun Subsystems.autoNavBarrel() = startChoreo("AutoNav Barrel") {
    choreography {
        val start = currentTime
        try {
            followTraj(Auto.AutoNav.barrel_traj)
        } finally {
            log(Debug) { "Finish in ${(currentTime - start).Second}s" }
        }
    }
}

suspend fun Subsystems.autoNavSlalom() = startChoreo("AutoNav Slalom") {
    choreography {
        val start = currentTime
        try {
            followTraj(Auto.AutoNav.slalom_traj)
        } finally {
            log(Debug) { "Finish in ${(currentTime - start).Second}s" }
        }
    }
}

suspend fun Subsystems.autoNavBounce() = startChoreo("AutoNav Bounce") {

    val robotPos by drivetrain.hardware.position.readEagerly().withoutStamps

    choreography {
        drivetrain.hardware.zeroOdometry()
        val bounceConfigs = with(Auto.AutoNav) { listOf(bounce1, bounce2, bounce3, bounce4) }
        val pathStarts = listOf(
            Position(0.Inch, 0.Inch, 0.Degree),
            Position(-43.5.Inch, 48.5.Inch, -115.Degree),
            Position(-43.5.Inch, 138.5.Inch, 90.Degree),
            Position(-43.5.Inch, 228.5.Inch, -90.Degree),
        )

        val start = currentTime
        try {
            for (i in 0 until 4) {
                val config = bounceConfigs[i].also { log(Debug) { "CONFIG: $it" } }
                val path = loadRobotPath(config.name) ?: run {
                    log(Error) { "Couldn't find path ${config.name}" }
                    return@choreography
                }
                drivetrain.followTrajectory(fastAsFuckPath(path, config), config, origin = pathStarts[i])
            }
        } finally {
            log(Debug) { "Finish in ${(currentTime - start).Second}s" }
        }

//        val bounceConfigs = with(Auto.AutoNav) { listOf(bounce1, bounce2, bounce3, bounce4) }
//        val pathThetas = listOf(-14.21.Degree, 90.Degree, 90.Degree, 90.Degree)
//        val origin = robotPos.copy()
//
//        var sum = Waypoint(0.Foot, 0.Foot)
//
//        val start = currentTime
//        try {
//            for (i in 0 until 4) {
//                val config = bounceConfigs[i].also { log(Debug) { "CONFIG: $it" } }
//                val path = loadRobotPath(config.name) ?: run {
//                    log(Error) { "Couldn't find path ${config.name}" }
//                    return@choreography
//                }
//
//                val traj = fastAsFuckPath(path, config)
//
//                val p = (RotationMatrix(origin.bearing) rz sum) + origin.vector
//                val pTheta = (origin.bearing `coterminal +` (pathThetas[i] - pathThetas[0]))
//                    .let { if (bounceConfigs[i].reverse) it `coterminal +` 180.Degree else it }
//
//                val pos = Position(p.x, p.y, pTheta)
//                drivetrain.log(Debug) { "Starting position #$i: $pos" }
//
//                drivetrain.followTrajectory(traj, config, origin = pos)
//
//                sum += RotationMatrix(pathThetas[i] - pathThetas[0]) rz traj.last().y
//            }
//        } finally {
//            log(Debug) { "Finish in ${(currentTime - start).Second}s" }
//        }
    }
}