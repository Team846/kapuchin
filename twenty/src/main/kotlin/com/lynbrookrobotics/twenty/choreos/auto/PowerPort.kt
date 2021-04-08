package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Auto.PowerPort
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

suspend fun Subsystems.powerPortTeleop() = startChoreo("Power Port Teleop") {

    val powerPortShoot by driver.powerPortShoot.readEagerly().withoutStamps
    val powerPortIntake by driver.powerPortIntake.readEagerly().withoutStamps
    val powerPortIntakeNoDrive by driver.powerPortIntakeNoDrive.readEagerly().withoutStamps
    val zeroOdometry by driver.zeroOdometry.readEagerly().withoutStamps

    val aimJobs = listOf(
        choreography {  },
        choreography { turret?.fieldOrientedPosition(drivetrain, PowerPort.goalPos) },
        choreography { turret?.trackTarget(limelight, flywheel!!, drivetrain )}
    )

    choreography {
        drivetrain.hardware.zeroOdometry()
        launch { flywheel?.set(PowerPort.shootSpeed) }

        launch { shooterHood?.set(Up) }

        val firstShotJob = launch {
            launch { powerPortShootSetup() }
            delay(1.Second)
            withTimeout(2.Second) {
                carousel.set(carousel.fireAllDutycycle)
            }
        }

        var aimJob: Job? = null

        runWhenever(
            { zeroOdometry } to {
                drivetrain.hardware.zeroOdometry()
                freeze()
            },
            { powerPortShoot } to {
                launch { powerPortShootSetup() }

//                val config = PowerPort.pathConfig.copy(reverse = false)
                drivetrain.followTrajectory(
                    fastAsFuckLine(PowerPort.distance, PowerPort.pathConfig.copy(reverse = false)),
                    PowerPort.pathConfig.copy(reverse = false)
                )
//                drivetrain.followTrajectory(drivetrain.trajectoryToWaypoint(PowerPort.shootPos, config), config)

                aimJob?.cancel()
                launch { turret?.manualOverride(operator) }
                delay(PowerPort.shootDelay)
                launch { carousel.set(carousel.fireAllDutycycle) }

                freeze()
            },

            { powerPortIntake } to {
                if (!firstShotJob.isCancelled) {
                    firstShotJob.cancel()
                }
                aimJob = launch { aimJobs[PowerPort.aimMode].invoke(this) }

                launch {
                    carousel.state.clear()
                    intakeBalls()
                }

                val config = PowerPort.pathConfig.copy(reverse = true)
                drivetrain.followTrajectory(
                    fastAsFuckLine(PowerPort.distance, PowerPort.pathConfig.copy(reverse = true)),
                    PowerPort.pathConfig.copy(reverse = true)
                )
                freeze()
            },

            { powerPortIntakeNoDrive } to {
                if (!firstShotJob.isCancelled) {
                    firstShotJob.cancel()
                }

                carousel.state.clear()
                intakeBalls()
            }
        )
    }
}

suspend fun Subsystems.powerPortShootSetup() {
    carousel.state.clear()
    carousel.state.push(3)
    carousel.state.shootInitialAngle()?.let { angle ->
        carousel.set(angle)
        feederRoller!!.set(feederRoller.feedSpeed)
    }
}

fun DrivetrainComponent.trajectoryToWaypoint(p: Waypoint, config: AutoPathConfiguration) = pathToTrajectory(
    nSect(hardware.position.optimizedRead(currentTime, hardware.syncThreshold).y.vector, p, 3.Inch),
    maxSpeed * config.speedFactor,
    maxOmega * config.percentMaxOmega * config.speedFactor,
    config.maxAccel,
    config.maxDecel,
    config.endingVelocity,
)