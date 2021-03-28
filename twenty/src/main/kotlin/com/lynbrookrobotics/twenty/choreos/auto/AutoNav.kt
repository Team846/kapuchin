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
//            var reverse = false
//
//            var delta = 74.89.Degree
//            val origin = robotPos.copy()
//            var startPos = robotPos.copy()
//            var prev: Waypoint? = null
//            trajectories.forEach { trajectory ->
//                prev?.let { p1 ->
//                    val matrix = RotationMatrix(origin.bearing)
//                    val p2 = (matrix rz p1) + startPos.vector
//
//                    startPos = Position(
//                        p2.x,
//                        p2.y,
//                        (startPos.bearing - delta).let { if (!reverse) 180.Degree `coterminal +` it else it })
//                    delta = 0.Degree
//                }
//                println("Start: $startPos")
//                drivetrain.followTrajectory(trajectory, Auto.AutoNav.bounce.copy(reverse = reverse), origin = startPos)
//
//                prev = trajectory.last().y
//                reverse = !reverse
//            }

            val pathThetas = listOf(-15.11.Degree, 90.Degree, 90.Degree, 90.Degree)
            val origin = robotPos.copy()

            var reverse = false
            var sum = Waypoint(0.Foot, 0.Foot)

            var i = 0
            trajectories.forEach { trajectory ->
                log(Debug) { "~~~~~~~~~~~~RUN #$i" }

                val matrix = RotationMatrix(origin.bearing)
                val p = (matrix rz sum) + origin.vector
                log(Debug) { "sum: ${sum.x.Foot},${sum.y.Foot}" }

                var pTheta = origin.bearing `coterminal +` pathThetas[i]
                if (reverse) pTheta = pTheta `coterminal +` 180.Degree
                val pos = Position(p.x, p.y, pTheta)
                log(Debug) { "Start: $pos" }
                drivetrain.followTrajectory(trajectory, Auto.AutoNav.bounce.copy(reverse = reverse), origin = pos)

                sum += RotationMatrix(pathThetas[i]) rz trajectory.last().y
                reverse = !reverse
                i += 1
            }
        }

        log(Debug) { "bounce finished: ${time.Second withDecimals 3}s" }
    }
}