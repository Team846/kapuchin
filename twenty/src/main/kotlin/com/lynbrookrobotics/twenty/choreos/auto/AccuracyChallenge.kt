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

suspend fun Subsystems.zoneMoveThenShoot(zoneTarget: Pair<L, AngularVelocity>, zone: Pair<String, String>, hoodTarget: ShooterHoodState = Up) {
    val speedFactor = 40.Percent

    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Finish Each Zone") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret!!.hardware.position.readEagerly().withoutStamps
        val accuracyChallenge by operator.accuracyChallenge.readEagerly().withoutStamps

        choreography {
            // Keep the intake out as long as balls < 3
            launch { runWhile({ carousel.state.balls < 3 }, choreography { intakeBalls() }) }

            // Drive to Zone
            val firstPathName = zone.first
            val firstPath = loadRobotPath(firstPathName)
            if (firstPath == null) {
                // Doesn't run the initial "drive to zone"
                if (firstPathName === "undefined") {
                    log(Debug) { "Initial Green Zone ... Skipping driving to zone"}
                }
                log(Error) { "Unable to find $firstPathName" }
            }

            firstPath?.let {
                drivetrain.followTrajectory(
                    fastAsFuckPath(it, speedFactor),
                    maxExtrapolate = drivetrain.maxExtrapolate,
                    reverse = true
                )
            }

            // Auto Aim Turret
            turret!!.set(turretPos - reading!!.tx.also { println(it) })

            // Wait to shoot until confirmation
            delayUntil { accuracyChallenge }

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
            launch { shooterHood?.set(hoodTarget) }
            delay(0.5.Second)
            launch { carousel.set(carousel.fireAllDutycycle) }
            delay(0.5.Second)

            // Drive back to re-intro zone
            val secondPathName = zone.second
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

suspend fun Subsystems.accuracyChallenge() {
    val greenToReIntroPath = "greenToReIntro"
    val reIntroToGreenPath = "reIntroToGreen"
    val yellowToReIntroPath = "yellowToReIntro"
    val reIntroToYellowPath = "reIntroToYellow"
    val blueToReIntroPath = "blueToReIntro"
    val reIntroToBluePath = "reIntroToBlue"
    val redToReIntroPath = "greenToReIntro"
    val reIntroToRedPath = "reIntroToRed"

    val greenPathWithoutStartDrive = "undefined" to greenToReIntroPath
    val greenPaths = reIntroToGreenPath to greenToReIntroPath
    val yellowPaths = reIntroToYellowPath to yellowToReIntroPath
    val bluePaths = reIntroToBluePath to blueToReIntroPath
    val redPaths = reIntroToRedPath to redToReIntroPath


    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run" }
        freeze()
    } else startChoreo("Accuracy Challenge") {
        val accuracyChallenge by operator.accuracyChallenge.readEagerly().withoutStamps

        choreography {
            // Reindex carousel
            carousel.rezero()

            // Shoot balls + drive back and forth for each zone (green + yellow + blue + red + red)
            launch { zoneMoveThenShoot(flywheel.greenZone, greenPathWithoutStartDrive, Down) }
            delayUntil { accuracyChallenge }
            launch { zoneMoveThenShoot(flywheel.yellowZone, yellowPaths) }
            delayUntil { accuracyChallenge }
            launch { zoneMoveThenShoot(flywheel.blueZone, bluePaths) }
            delayUntil { accuracyChallenge }
            launch { zoneMoveThenShoot(flywheel.redZone, redPaths) }
            delayUntil { accuracyChallenge }
            launch { zoneMoveThenShoot(flywheel.redZone, redPaths) }
        }
    }
}