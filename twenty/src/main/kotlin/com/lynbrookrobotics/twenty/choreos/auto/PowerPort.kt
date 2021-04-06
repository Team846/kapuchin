package com.lynbrookrobotics.twenty.choreos.auto

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Auto
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.intakeBalls
import com.lynbrookrobotics.twenty.routines.followTrajectory
import com.lynbrookrobotics.twenty.routines.manualOverride
import com.lynbrookrobotics.twenty.routines.rezero
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.Up
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun Subsystems.powerPortTeleop() = startChoreo("Power Port Teleop") {

    val powerPortShoot by driver.powerPortShoot.readEagerly().withoutStamps
    val powerPortIntake by driver.powerPortIntake.readEagerly().withoutStamps
    val powerPortIntakeNoDrive by driver.powerPortIntakeNoDrive.readEagerly().withoutStamps
    val zeroOdometry by driver.zeroOdometry.readEagerly().withoutStamps

    choreography {
//        if (turret != null && !turret.hardware.isZeroed) launch {
//            log(Debug) { "Rezeroing turret" }
//            turret.rezero(electrical)
//            turret.hardware.zero()
//        }
//        turret?.set(0.Degree, 2.Degree)

        drivetrain.hardware.zeroOdometry()
        launch { flywheel?.set(Auto.PowerPort.shootSpeed) }
        launch { turret?.manualOverride(operator) }
//        launch { turret?.fieldOrientedPosition(drivetrain, UomVector(0.Foot, Auto.PowerPort.goalDistance))}
        launch { shooterHood?.set(Up) }

        val j = launch {
            launch { powerPortShootSetup() }
            delay(1.Second)
            withTimeout(2.Second) {
                carousel.set(carousel.fireAllDutycycle)
            }
        }

        runWhenever(
            { zeroOdometry } to {
                drivetrain.hardware.zeroOdometry()
                freeze()
            },
            { powerPortShoot } to {
                launch { powerPortShootSetup() }
                drivetrain.followTrajectory(
                    fastAsFuckLine(Auto.PowerPort.distance, Auto.PowerPort.pathConfig.copy(reverse = false)),
                    Auto.PowerPort.pathConfig.copy(reverse = false)
                )
                delay(Auto.PowerPort.shootDelay)
                launch { carousel.set(carousel.fireAllDutycycle) }
                freeze()
            },

            { powerPortIntake } to {
                if (!j.isCancelled) {
                    j.cancel()
                }

                launch {
                    carousel.state.clear()
                    intakeBalls()
                }

                drivetrain.followTrajectory(
                    fastAsFuckLine(Auto.PowerPort.distance, Auto.PowerPort.pathConfig.copy(reverse = true)),
                    Auto.PowerPort.pathConfig.copy(reverse = true)
                )
                freeze()
            },

            { powerPortIntakeNoDrive } to {
                if (!j.isCancelled) {
                    j.cancel()
                }

                launch {
                    carousel.state.clear()
                    intakeBalls()
                }
                freeze()
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