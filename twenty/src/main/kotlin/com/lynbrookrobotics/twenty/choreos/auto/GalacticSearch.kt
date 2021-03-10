package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.*
import com.lynbrookrobotics.twenty.subsystems.intake.*
import edu.wpi.first.networktables.NetworkTableInstance
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.twenty.routines.followTrajectory
import com.lynbrookrobotics.twenty.routines.optimalEat
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log
import kotlin.system.measureTimeMillis

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis


private fun getPathToRun(): Int{
    NetworkTableInstance.getDefault().getTable("/SmartDashboard").getEntry("path").setNumber(0)
    var path =  NetworkTableInstance.getDefault().getTable("/SmartDashboard").getEntry("path").toString().toInt()

    while(path == 0){ //waits for jetson to detect red
        println("Waiting for Jetson to run")
        path =  NetworkTableInstance.getDefault().getTable("/SmartDashboard").getEntry("path").toString().toInt()
    }
    return path
}

/**
 * Autonomous routing for Galactic Search challenge
 *
 * @author Kaustubh Khulbe
 */
suspend fun Subsystems.galacticSearch(){
    val speedFactor = 40.Percent
    var pathName = ""
    //val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    startChoreo("Galactic Search"){
        choreography {

            carousel.rezero()
            val intakeJob = launch { intakeBalls() }
            if (getPathToRun() == 1) pathName = "red"
            else pathName = "blue"

            val path = loadRobotPath(pathName)
            if (path == null) {
                log(Error) { "Unable to find $pathName" }
            }

            path?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, speedFactor),
                    maxExtrapolate = drivetrain.maxExtrapolate,
                    reverse = true
                )
            }

            intakeJob.cancel()
        }

    }
    //High level function for path
}
