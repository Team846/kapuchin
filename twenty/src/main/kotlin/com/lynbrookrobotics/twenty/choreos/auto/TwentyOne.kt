package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Auto
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.fieldOrientedPosition
import com.lynbrookrobotics.twenty.routines.followTrajectory
import com.lynbrookrobotics.twenty.routines.rezero
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

suspend fun Subsystems.judgedAuto() {
    val flywheelTarget = 6000.Rpm
    val speedFactor = 40.Percent
    val pathName = "judged_arc1"
    val targetDist = 26.Foot

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run auto." }
        freeze()
    } else startChoreo("Judged Auto") {

        val drivetrainPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret!!.hardware.position.readEagerly().withoutStamps

        choreography {
            // Approximately aim towards target
            val j = launch {
                turret?.fieldOrientedPosition(
                    drivetrain,
                    UomVector(
                        drivetrainPosition.x + targetDist * sin(drivetrainPosition.bearing - 90.Degree),
                        drivetrainPosition.y + targetDist * cos(drivetrainPosition.bearing - 90.Degree)
                    )
                )
            }

            // Reindex carousel
            carousel.rezero()
//            carousel.whereAreMyBalls()

            // Bring out intake
            val intakeJob = launch { intakeBalls() }

            // Follow path (if exists)
            val path = loadRobotPath(pathName)
            if (path == null) {
                log(Error) { "Unable to find $pathName" }
            }

            path?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, Auto.defaultPathConfig),
                    Auto.defaultPathConfig,
                )
            }

            // Stop intake
            intakeJob.cancel()
            carousel.state.clear()
            carousel.state.push(3)

//            // Precise aim with limelight
            j.cancel()
            log(Debug) { "Field Oriented Cancelled" }


            coroutineContext.job.cancelChildren()
            delay(1.Second)
            turret!!.set(turretPos - reading!!.tx.also { println(it) })
//            delay(0.5.Second)
//            withTimeout(1.Second) {
//                turret!!.set(turretPos - reading!!.tx.also { println(it) })
//            }

            // Set carousel initial
            val initialAngle = carousel.state.shootInitialAngle()
            if (initialAngle != null) {
                carousel.set(initialAngle)
            } else {
                log(Error) { "No balls to shoot" }
                coroutineContext.job.cancelChildren()
                return@choreography
            }

            // Set flywheel and feeder roller
            launch { flywheel.set(flywheelTarget) }
            launch { feederRoller.set(feederRoller.feedSpeed) }

            delay(2.Second)
//            withTimeout(3.Second) {
//                delayUntilFeederAndFlywheel(flywheelTarget)
//            } ?: run {
//                log(Error) { "Feeder flywheel not set" }
//                coroutineContext.job.cancelChildren()
//                return@choreography
//            }

            log(Debug) { "Feeder roller and flywheel set, aim complete" }
            launch { shooterHood?.set(Up) }
            delay(0.5.Second)
            launch { carousel.set(carousel.fireAllDutycycle) }
        }
    }
}
