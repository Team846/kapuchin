package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

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
            withTimeout(AutoPrefs.getOffLineTimeout) { autoFire(flywheel.presetLow) }
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
                autoFire(flywheel.presetLow)
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
                autoFire(flywheel.presetLow)
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
                autoFire(flywheel.presetAnitez)
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
                autoFire(flywheel.presetAnitez)
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
                autoFire(flywheel.presetLow)
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
            autoFire(flywheel.presetMed)
        }
    }
}

private suspend fun Subsystems.autoFire(speed: AngularVelocity) {
    if (turret == null) {
        log(Error) { "need turret" }
    } else startChoreo("Auto Fire") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret.hardware.position.readEagerly().withoutStamps

        choreography {
            val j1 = launch { flywheel?.set(speed) }
            val j2 = launch { feederRoller?.set(feederRoller.feedSpeed) }
            val j3 = launch { shooterHood?.set(ShooterHoodState.Up) }

            delay(1.Second)
            withTimeout(2.Second) {
                reading?.let { snapshot ->
                    turret.set(
                        turretPos - snapshot.tx + limelight.hardware.conversions.mountingBearing,
                        1.Degree
                    )
                }
            }

            delay(1.Second)

            withTimeout(AutoPrefs.shootTime) { carousel.set(carousel.fireAllDutycycle) }
            j1.cancel()
            j2.cancel()
            j3.cancel()

            // reset carousel
            carousel.state.clear()
            carousel.rezero()
            carousel.hardware.encoder.position = 0.0

            // ready to intake
            carousel.state.intakeAngle()?.let { carousel.set(it) }
        }
    }
}

private suspend fun Subsystems.autoDriveLine(distance: Length, reverse: Boolean) {
    val config = AutoPrefs.genericLinePathConfig.copy(reverse = reverse)
    drivetrain.followTrajectory(fastAsFuckLine(distance, config), config)
}