package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.auto.AutoPrefs.aimError
import com.lynbrookrobotics.twenty.choreos.auto.AutoPrefs.defaultRPM
import com.lynbrookrobotics.twenty.choreos.auto.AutoPrefs.shootTime
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import java.awt.Color

object AutoPrefs : Named by Named("Auto") {
    val shootTime by pref(2, Second)
    val getOffLineTimeout by pref(12, Second)

    val genericLinePathConfig by autoPathConfigPref("")
    val L1I1PathConfig by autoPathConfigPref("L1I1", defaultReverse = true)

    val getOffLineDistance by pref(3, Foot)
    val L2I1Distance by pref(16, Foot)
    val I1S1Distance by pref(6, Foot)

    val L1TurretPos by pref(0, Degree)
    val L2TurretPos by pref(-30, Degree)
    val S1TurretPos by pref(-15, Degree)

    val aimError by pref(0.5, Degree)
    val defaultRPM by pref (5500, Rpm)
}

suspend fun Subsystems.autoGetOffLine() = startChoreo("Auto Drive") {
    choreography {
        launch {
            carousel.rezero()
        }
        autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
    }
}

suspend fun Subsystems.autoShootGetOffLine() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto Shoot GetOffLine") {
        choreography {
            delay(1.Second)
            withTimeout(AutoPrefs.getOffLineTimeout) { autoFire() }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.autoL1ShootGetOffLine() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot GetOffLine") {
        choreography {
            delay(1.Second)

            withTimeout(AutoPrefs.getOffLineTimeout) {
                turret?.set(AutoPrefs.L1TurretPos, 5.Degree)
                autoFire()
            }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.autoL2ShootGetOffLine() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot GetOffLine") {
        choreography {
            delay(1.Second)

            withTimeout(AutoPrefs.getOffLineTimeout) {
                turret?.set(AutoPrefs.L2TurretPos, 5.Degree)
                autoFire()
            }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.autoL1ShootI1IntakeS1Shoot() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot I1Intake S1Shoot") {
        choreography {
            delay(1.Second)
            withTimeout(AutoPrefs.getOffLineTimeout) {
                turret?.set(AutoPrefs.L1TurretPos, 5.Degree)
                autoFire()
            }

            loadRobotPath(AutoPrefs.L1I1PathConfig.name)?.let { path ->
                // intake and go to I1
                val intakeJob = launch { intakeBalls() }
                drivetrain.followTrajectory(fastAsFuckTrajectory(path, AutoPrefs.L1I1PathConfig),
                    AutoPrefs.L1I1PathConfig)
                intakeJob.cancel()

                // go to S1
                val turretJob = launch { turret?.set(AutoPrefs.S1TurretPos, 5.Degree) }
                autoDriveLine(AutoPrefs.I1S1Distance, reverse = false)
                turretJob.join()

                // shoot
                autoFire()
            } ?: autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.autoL2ShootI1IntakeS1Shoot() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot I1Intake S1Shoot") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                turret?.set(AutoPrefs.L2TurretPos, 5.Degree)
                autoFire()
            }

            // intake and go to I1
            val intakeJob = launch { intakeBalls() }
            autoDriveLine(AutoPrefs.L2I1Distance, reverse = true)
            intakeJob.cancel()

            // go to S1
            val turretJob = launch { turret?.set(AutoPrefs.S1TurretPos, 5.Degree) }
            autoDriveLine(AutoPrefs.I1S1Distance, reverse = false)
            turretJob.join()

            // shoot
            autoFire()
        }
    }
}

suspend fun Subsystems.autoFire() {
    if (flywheel == null || feederRoller == null || turret == null) log(Error) { "Requires flywheel, feederRoller, and turret" }
    else startChoreo("shoot all") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val pitch by drivetrain.hardware.pitch.readEagerly().withoutStamps
        var target = 0.Rpm

        choreography {
            launch { feederRoller.set(0.Rpm) }

            carousel.rezero()
            carousel.set(carousel.state.shootInitialAngle() ?: carousel.hardware.nearestSlot())

            launch { feederRoller.set(feederRoller.feedSpeed) }

            launch {
                val distance = reading?.let { limelight.hardware.conversions.distanceToGoal(it, pitch) } //may need to be 0.Degree
                target = distance?.let { flywheel.hardware.conversions.rpmCurve(it) } ?: defaultRPM
                flywheel.set(target)
            }

            launch {
                turret.trackTarget(drivetrain, limelight)
            }

            delayUntil { flywheelSpeed in target `±` flywheel.tolerance && reading!!.tx.abs < aimError }

            val j = launch { shooterHood?.set(ShooterHoodState.Up) }
            launch { leds?.set(Color.GREEN) }
            delay(0.3.Second)

            withTimeout(shootTime) { carousel.set(carousel.shootFastSpeed) }

            launch {
                delay(0.3.Second)
                j.cancel()
            }

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