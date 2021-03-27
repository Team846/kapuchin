package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.followTrajectory
import com.lynbrookrobotics.twenty.routines.rezero
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.launch

private fun getPathToRun(): Int {
    SmartDashboard.getEntry("path").setNumber(0)
    var path = SmartDashboard.getEntry("path").toString().toInt()

    while (path == 0) { // waits for jetson to detect red
        println("Waiting for Jetson to run")
        path = SmartDashboard.getEntry("path").toString().toInt()
    }
    return path
}

/**
 * Autonomous routing for Galactic Search challenge
 *
 * @author Kaustubh Khulbe
 */
suspend fun Subsystems.galacticSearch() {
    var pathName = ""
    // val speedFactor = 20.Percent
    // val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    startChoreo("Galactic Search") {
        choreography {
            carousel.rezero()
            val pathRun = getPathToRun()
            val intakeJob = launch { intakeBalls() }

            if (pathRun == 1) pathName = "GalacticSearch_A_RED.tsv"
            else if (pathRun == 2) pathName = "GalacticSearch_A_BLUE.tsv"
            else if (pathRun == 3) pathName = "GalacticSearch_B_RED.tsv"
            else if (pathRun == 4) pathName = "GalacticSearch_B_BLUE.tsv"
            else log(Error) { "no path number recieved from getPathToRun()" }

            val path = loadRobotPath(pathName)
            if (path == null) {
                log(Error) { "Unable to find $pathName" }
            }

            path?.let {
                drivetrain.followTrajectory(
                        fastAsFuckPath(it, drivetrain.speedFactor),
                        maxExtrapolate = drivetrain.maxExtrapolate,
                        reverse = true)
            }

            intakeJob.cancel()
        }
    }
    // High level function for path
}
