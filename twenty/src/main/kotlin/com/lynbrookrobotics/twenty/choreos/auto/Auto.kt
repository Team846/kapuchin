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
import kotlinx.coroutines.launch
import java.awt.Color
import java.io.File

object AutoPrefs : Named by Named("Auto") {
    val shootTime by pref(2, Second)
    val getOffLineTimeout by pref(12, Second)
    val initialDelay by pref(0, Second)

    val genericLinePathConfig by autoPathConfigPref("")

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
        autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
    }
}

suspend fun Subsystems.auto3Ball() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto 3 ball") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val j = launch { flywheel.set(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                j.cancel()
            }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.auto6Ball(initialBearing: Angle) {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot I1Intake S1Shoot") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                val flywheelJob = launch { flywheel.set(flywheel.presetClose) }
                autoFire(flywheel.presetClose)
                flywheelJob.cancel()
            }

            // intake and go to I1
            val intakeJob = launch { intakeBalls() }
            val file = File("/home/lvuser/6_Ball_Auto.tsv")
//            autoDriveLine(AutoPrefs.L2I1Distance, reverse = true, initialBearing)
            autoDriveTraj(file, reverse = true, initialBearing)
            intakeJob.cancel()

            // go to S1
            val flywheelJob = launch { flywheel.set(flywheel.presetClose) }
            autoDriveLine(AutoPrefs.I1S1Distance, reverse = false, initialBearing)

            // shoot
            autoFire(flywheel.presetMed)
            flywheelJob.cancel()
        }
    }
}

suspend fun Subsystems.autoFire(flywheelPreset: AngularVelocity) {
    if (flywheel == null) log(Error) { "Requires flywheel" }
    else startChoreo("Auto Fire") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps

        choreography {
            var flywheelTarget = flywheelPreset

            val jobs = mutableListOf(
                launch { feederRoller?.set(feederRoller.feedSpeed) },
                launch {
                    val snapshot = reading?.copy()
                    if (snapshot != null) {
                        val target = targetFlywheelSpeed(flywheel, snapshot)

                        if ((target - flywheelPreset).abs > 2000.Rpm) {
                            log(Error) { "Calculated target (${target.Rpm} rpm) differs greatly from preset (${flywheelPreset.Rpm} rpm)" }
                        } else {
                            flywheelTarget = target
                            launch { leds?.blink(Color.BLUE) }
                            flywheel.set(target)
                        }
                    } else {
                        leds?.set(Color.RED)
                    }
                },
                launch { shooterHood?.set(ShooterHoodState.Up) }
            )

            delay(500.milli(Second))
            println("DELAYING")
            withTimeout(3.Second) {
                delayUntil {
                    flywheelSpeed in flywheelTarget `±` flywheel.tolerance
                            && reading?.let { it.tx < AutoPrefs.aimTolerance } ?: false
                }
            }

            if (reading?.let { it.tx < AutoPrefs.aimTolerance } == true) {
                jobs.add(launch { leds?.set(Color.GREEN) })
                withTimeout(shootTime) { carousel.set(carousel.shootSlowSpeed) }
            } else {
                scope.launch {
                    withTimeout(2.Second) {
                        leds?.blink(Color.RED)
                    }
                }
            }

            jobs.forEach { it.cancel() }
            carousel.state.clear()
            carousel.rezero()
            carousel.hardware.encoder.position = 0.0
        }
    }
}

private suspend fun Subsystems.autoDriveLine(distance: Length, reverse: Boolean, initialBearing: Angle? = null) {
    val config = AutoPrefs.genericLinePathConfig.copy(reverse = reverse)
    var origin = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y

    initialBearing?.let { origin = origin.copy(bearing = it) }
    drivetrain.followTrajectory(fastAsFuckLine(distance, config), config, origin = origin)
}

private suspend fun Subsystems.autoDriveTraj(trajectoryFile: File, reverse: Boolean, initialBearing: Angle? = null) {
    val config = AutoPrefs.genericLinePathConfig.copy(reverse = reverse)
    var origin = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y
    val traj = trajectoryFile
        .bufferedReader()
        .lineSequence()
        .drop(1)
        .map { it.split('\t') }
        .map { it.map { tkn -> tkn.trim() } }
        .map { Waypoint(it[0].toDouble().Foot, it[1].toDouble().Foot) stampWith it[2].toDouble().Second }
        .toList()
    initialBearing?.let { origin = origin.copy(bearing = it) }
    drivetrain.followTrajectory(traj, config)
}