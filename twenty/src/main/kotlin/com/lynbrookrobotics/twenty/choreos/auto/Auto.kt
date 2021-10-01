package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object AutoPrefs : Named by Named("Auto") {
    val shootTime by pref(2, Second)
    val getOffLineTimeout by pref(12, Second)

    val genericLinePathConfig by autoPathConfigPref("")
    val L1I1PathConfig by autoPathConfigPref("L1I1", defaultReverse = true)

    val getOffLineDistance by pref(3, Foot)
    val I1S1Distance by pref(6, Foot)

    val L1TurretPos by pref(0, Degree)
    val S1TurretPos by pref(-20, Degree)
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
            withTimeout(AutoPrefs.getOffLineTimeout) { autoFire(flywheel.presetLow) }
            autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

suspend fun Subsystems.autoL1ShootI1IntakeS1Shoot() {
    if (flywheel == null) {
        log(Error) { "Requires flywheel" }
    } else startChoreo("Auto L1Shoot I1Intake S1Shoot") {
        choreography {
            withTimeout(AutoPrefs.getOffLineTimeout) {
                turret?.set(AutoPrefs.L1TurretPos, 3.Degree)
                autoFire(flywheel.presetLow)
            }

            loadRobotPath(AutoPrefs.L1I1PathConfig.name)?.let { path ->
                // intake and go to I1
                val intakeJob = launch { intakeBalls() }
                drivetrain.followTrajectory(fastAsFuckTrajectory(path, AutoPrefs.L1I1PathConfig),
                    AutoPrefs.L1I1PathConfig)
                intakeJob.cancel()

                // go to S1
                val turretJob = launch { turret?.set(AutoPrefs.S1TurretPos, 3.Degree) }
                autoDriveLine(AutoPrefs.I1S1Distance, reverse = false) // worried robot wont go straight if it ended prev path off
                turretJob.join()

                // shoot
                autoFire(flywheel.presetMed)
            } ?: autoDriveLine(AutoPrefs.getOffLineDistance, reverse = true)
        }
    }
}

private suspend fun Subsystems.autoFire(speed: AngularVelocity) = coroutineScope {
    val spinUpJobs = arrayOf(
        launch { visionAimTurret() },
        launch { flywheel?.set(speed) },
        launch { feederRoller?.set(feederRoller.feedSpeed) },
        launch { shooterHood?.set(ShooterHoodState.Up) },
    )

    delayUntilFeederAndFlywheel(speed)

    withTimeout(AutoPrefs.shootTime) { carousel.set(carousel.fireAllDutycycle) }
    spinUpJobs.forEach { it.cancel() }

    // reset carousel
    carousel.state.clear()
    carousel.rezero()
    carousel.hardware.encoder.position = 0.0

    // ready to intake
    carousel.state.intakeAngle()?.let { carousel.set(it) }
}

private suspend fun Subsystems.autoDriveLine(distance: Length, reverse: Boolean) {
    val config = AutoPrefs.genericLinePathConfig.copy(reverse = reverse)
    drivetrain.followTrajectory(fastAsFuckLine(distance, config), config)
}