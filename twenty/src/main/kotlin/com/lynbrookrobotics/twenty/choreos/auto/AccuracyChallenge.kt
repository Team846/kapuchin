package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.*
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.carousel.*
import com.lynbrookrobotics.twenty.subsystems.intake.*
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

suspend fun Subsystems.zoneMoveThenShoot(zoneTarget: Pair<L, AngularVelocity>, Zone: Pair<String, String>) {
    val speedFactor = 40.Percent

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Finish Each Zone") {
        choreography {

            // Keep the intake as long as balls < 3
            launch { runWhile({ carousel.state.balls < 3 }, choreography { intakeBalls() }) }

            // Drive to Zone
            val firstPathName = Zone.first
            val firstPath = loadRobotPath(firstPathName)
            if (firstPath == null) {
                log(Error) { "Unable to find $firstPathName" }
            }

            firstPath?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, speedFactor),
                    maxExtrapolate = drivetrain.maxExtrapolate,
                    reverse = true
                )
            }

            // Set carousel initial
            val initialAngle = carousel.state.shootInitialAngle()
            if (initialAngle != null) {
                carousel.set(initialAngle)
            } else {
                log(Error) { "No balls to shoot" }
                coroutineContext.job.cancelChildren()
                return@choreography
            }

            // Spin up the flywheel and feeder roller
            launch { flywheel.set(zoneTarget.second) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(Up) }
            delay(0.5.Second)
            launch { carousel.set(carousel.fireAllDutycycle) }

            // Drive back to re-intro zone
            val secondPathName = Zone.second
            val secondPath = loadRobotPath(secondPathName)
            if (secondPath == null) {
                log(Error) { "Unable to find $secondPathName" }
            }

            secondPath?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, speedFactor),
                    maxExtrapolate = drivetrain.maxExtrapolate,
                    reverse = true
                )
            }

        }
    }
}

suspend fun Subsystems.AccuracyChallenge() {
    val speedFactor = 40.Percent

    val greenToReIntroPath = "greenToReIntro"
    val reIntroToGreenPath = "reIntroToGreen"
    val yellowToReIntroPath = "yellowToReIntro"
    val reIntroToYellowPath = "reIntroToYellow"
    val blueToReIntroPath = "blueToReIntro"
    val reIntroToBluePath = "reIntroToBlue"
    val redToReIntroPath = "greenToReIntro"
    val reIntroToRedPath = "reIntroToRed"

    val greenPaths = reIntroToGreenPath to greenToReIntroPath
    val yellowPaths = reIntroToYellowPath to yellowToReIntroPath
    val bluePaths = reIntroToBluePath to blueToReIntroPath
    val redPaths = reIntroToRedPath to redToReIntroPath


    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Accuracy Challenge") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret!!.hardware.position.readEagerly().withoutStamps

        choreography {

            // Reindex carousel
            carousel.rezero()

            // Auto Aim Turret
            turret!!.set(turretPos - reading!!.tx.also { println(it) })

            // Set carousel initial
            val initialAngle = carousel.state.shootInitialAngle()
            if (initialAngle != null) {
                carousel.set(initialAngle)
            } else {
                log(Error) { "No balls to shoot" }
                coroutineContext.job.cancelChildren()
                return@choreography
            }

            // Green Zone Starting (Shoot + drive back)

            launch { flywheel.set(flywheel.greenZone.second) }
            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { shooterHood?.set(Up) }
            delay(0.5.Second)
            launch { carousel.set(carousel.fireAllDutycycle) }

            // Follow path (if exists)
            val pathName = greenPaths.second
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


            // Shoot balls for each zone (yellow + blue + red + red)
            launch { zoneMoveThenShoot(flywheel.yellowZone, yellowPaths) }
            launch { zoneMoveThenShoot(flywheel.blueZone, bluePaths) }
            launch { zoneMoveThenShoot(flywheel.redZone, redPaths) }
            launch { zoneMoveThenShoot(flywheel.redZone, redPaths) }
        }
    }
}