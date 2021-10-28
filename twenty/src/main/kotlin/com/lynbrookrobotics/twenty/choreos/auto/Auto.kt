package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.math.*
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
import kotlinx.coroutines.launch
import java.awt.Color

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
            withTimeout(AutoPrefs.getOffLineTimeout) { autoFire(flywheel.presetClose) }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.auto6Ball() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot I1Intake S1Shoot") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) { autoFire(flywheel.presetClose) }

            // intake and go to I1
            val intakeJob = launch { intakeBalls() }
            autoDriveLine(AutoPrefs.L2I1Distance, reverse = true)
            intakeJob.cancel()

            // go to S1
            autoDriveLine(AutoPrefs.I1S1Distance, reverse = false)

            // shoot
            autoFire(flywheel.presetMed)
        }
    }
}

suspend fun Subsystems.autoFire(flywheelPreset: AngularVelocity) {
    if (flywheel == null) log(Error) { "Requires flywheel" }
    else startChoreo("Auto Fire") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps

        choreography {
            val snapshot = reading?.copy()

            var flywheelTarget = flywheelPreset

            val jobs = mutableListOf(
                launch { feederRoller?.set(feederRoller.feedSpeed) },
                launch {
                    snapshot?.let {
                        val target = targetFlywheelSpeed(flywheel, it)

                        if ((target - flywheelPreset).abs > 2000.Rpm) {
                            log(Error) { "Calculated target (${target.Rpm} rpm) differs greatly from preset (${flywheelPreset.Rpm} rpm)" }
                        } else {
                            flywheelTarget = target
                            leds?.blink(Color.BLUE)
                            flywheel.set(target)
                        }
                    } ?: leds?.set(Color.RED)
                },
                launch { shooterHood?.set(ShooterHoodState.Up) }
            )

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

private suspend fun Subsystems.autoDriveLine(distance: Length, reverse: Boolean) {
    val config = AutoPrefs.genericLinePathConfig.copy(reverse = reverse)
    drivetrain.followTrajectory(fastAsFuckLine(distance, config), config)
}