package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.auto.AutoPrefs.shootTime
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import com.lynbrookrobotics.twenty.subsystems.shooter.targetFlywheelSpeed
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import java.awt.Color
import java.io.File

object AutoPrefs : Named by Named("Auto") {
    val shootTime by pref(2, Second)
    val getOffLineTimeout by pref(12, Second)
    val initialDelay by pref(0, Second)

    val slowLineConfig by autoPathConfigPref("")
    val fastLineConfig by autoPathConfigPref("")
    val L1I1Config by autoPathConfigPref("", defaultReverse = true)

    val getOffLineDistance by pref(3, Foot)
    val L2I1Distance by pref(16, Foot)
    val I1S1Distance by pref(6, Foot)

    val aimTolerance by pref(5, Degree)
}

suspend fun Subsystems.autoGetOffLine() = startChoreo("Auto Drive") {
    choreography {
        launch {
            carousel.rezero()
            carousel.hardware.encoder.position = 0.0
        }
        autoDriveLine(AutoPrefs.getOffLineDistance, AutoPrefs.fastLineConfig.copy(reverse = true))
    }
}

suspend fun Subsystems.auto3BallReverse() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto 3 Ball Reverse") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val spinJob = launch { autoSpinUp(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                spinJob.cancel()
            }
            autoDriveLine(AutoPrefs.getOffLineDistance, AutoPrefs.fastLineConfig.copy(reverse = true))
        }
    }
}

suspend fun Subsystems.auto3BallForward() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto 3 Ball Forward") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val spinJob = launch { autoSpinUp(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                spinJob.cancel()
            }
            autoDriveLine(AutoPrefs.getOffLineDistance, AutoPrefs.fastLineConfig.copy(reverse = false))
        }
    }
}

suspend fun Subsystems.auto6BallStraight(initialBearing: Angle) {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto 6 Ball Straight") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val spinJob = launch { autoSpinUp(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                spinJob.cancel()
            }

            val intakeJob = launch { intakeBalls() }
            autoDriveLine(AutoPrefs.L2I1Distance, AutoPrefs.slowLineConfig.copy(reverse = true), initialBearing)
            intakeJob.cancel()

            val spinJob = launch { autoSpinUp(flywheel.presetMed) }
            autoDriveLine(AutoPrefs.I1S1Distance, AutoPrefs.fastLineConfig.copy(reverse = false), initialBearing)
            autoFire(flywheel.presetMed)
            spinJob.cancel()
        }
    }
}

suspend fun Subsystems.auto6BallCurved(initialBearing: Angle) {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto 6 Ball Curved") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val spinJob = launch { autoSpinUp(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                spinJob.cancel()
            }

            val intakeJob = launch { intakeBalls() }
            val file = File("/home/lvuser/6_Ball_Auto.tsv")
            autoDriveTraj(file, AutoPrefs.L1I1Config, initialBearing)
            intakeJob.cancel()

            val spinJob = launch { autoSpinUp(flywheel.presetMed) }
            autoDriveLine(AutoPrefs.I1S1Distance, AutoPrefs.fastLineConfig.copy(reverse = false), initialBearing)
            autoFire(flywheel.presetMed)
            spinJob.cancel()
        }
    }
}

suspend fun Subsystems.autoSpinUp(flywheelPreset: AngularVelocity) = coroutineScope {
    launch { flywheel?.set(flywheelPreset) }
    launch { feederRoller?.set(feederRoller.feedSpeed) }
    launch { shooterHood?.set(ShooterHoodState.Up) }
}

suspend fun Subsystems.autoFire(flywheelPreset: AngularVelocity) {
    if (flywheel == null) log(Error) { "Requires flywheel" }
    else startChoreo("Auto Fire") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps

        choreography {
            var flywheelTarget = flywheelPreset

            val snapshot = reading?.copy()
            if (snapshot != null) {
                val target = targetFlywheelSpeed(flywheel, snapshot)

                if (target !in 5000.Rpm `±` 2000.Rpm) {
                    log(Error) { "Calculated target (${target.Rpm} rpm) is too far off" }
                    launch { leds?.set(Color.RED) }
                } else {
                    launch { leds?.blink(Color.BLUE) }
                    flywheelTarget = target
                    launch { flywheel.set(target) }
                }
            } else {
                launch { leds?.set(Color.RED) }
            }

            delay(500.milli(Second))
            withTimeout(2.Second) {
                delayUntil {
                    flywheelSpeed in flywheelTarget `±` flywheel.tolerance
                            && reading?.let { it.tx < AutoPrefs.aimTolerance } != false
                }
            }

            if (reading?.let { it.tx < AutoPrefs.aimTolerance } != false) {
                launch { leds?.set(Color.GREEN) }
                withTimeout(shootTime) { carousel.set(carousel.shootFastSpeed) }
            } else {
                scope.launch {
                    withTimeout(2.Second) {
                        leds?.blink(Color.RED)
                    }
                }
            }

            coroutineContext[Job]!!.cancelChildren()

            carousel.state.clear()
            carousel.rezero()
            carousel.hardware.encoder.position = 0.0
        }
    }
}

private suspend fun Subsystems.autoDriveLine(
    distance: Length,
    config: AutoPathConfiguration,
    initialBearing: Angle? = null,
) {
    var origin = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y

    initialBearing?.let { origin = origin.copy(bearing = it) }
    drivetrain.followTrajectory(fastAsFuckLine(distance, config), config, origin = origin)
}

private suspend fun Subsystems.autoDriveTraj(
    trajectoryFile: File,
    config: AutoPathConfiguration,
    initialBearing: Angle? = null,
) {
    var origin = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y
    val traj = trajectoryFile
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[1].toDouble().Foot * -1, it[0].toDouble().Foot) stampWith it[2].toDouble().Second }
        .toList()
    initialBearing?.let { origin = origin.copy(bearing = it) }
    drivetrain.followTrajectory(traj, config)
}