package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.LogLevel.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.carousel.*
import com.lynbrookrobotics.twenty.subsystems.intake.*
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun Subsystems.digestionTeleop() = startChoreo("Digestion Teleop") {

    val intakeBalls by driver.intakeBalls.readEagerly().withoutStamps
    val unjamBalls by driver.unjamBalls.readEagerly().withoutStamps

    val aim by operator.aim.readEagerly().withoutStamps
    val hoodUp by operator.hoodUp.readEagerly().withoutStamps
    val shoot by operator.shoot.readEagerly().withoutStamps

    val shooterPreset by operator.shooterPreset.readEagerly().withoutStamps

    val rezeroTurret by operator.rezeroTurret.readEagerly().withoutStamps
    val reindexCarousel by operator.reindexCarousel.readEagerly().withoutStamps

    val turretManual by operator.turretManual.readEagerly().withoutStamps

    val ball0 by operator.ball0.readEagerly().withoutStamps
    val ball1 by operator.ball1.readEagerly().withoutStamps
    val ball2 by operator.ball2.readEagerly().withoutStamps
    val ball3 by operator.ball3.readEagerly().withoutStamps

    choreography {
        if (turret != null && !turret.hardware.isZeroed) launch {
            log(INFO) { "Rezeroing turret" }
            turret.rezero(electrical)
        }

//        withTimeout(15.Second) {
//            log(Debug) { "Reindexing carousel" }
//            carousel.rezero()
//            carousel.whereAreMyBalls()
//        }

        launch {
            launchWhenever(
//                { turret?.routine == null } to choreography { turret?.fieldOrientedPosition(drivetrain) },
            )
        }

        runWhenever(
            { intakeBalls } to choreography { intakeBalls() },
            { unjamBalls } to choreography { intakeRollers?.set(intakeRollers.pukeSpeed) ?: freeze() },

            { aim } to choreography { visionAimTurret() },
            { hoodUp } to choreography { shooterHood?.set(Up) ?: freeze() },
            { shoot } to choreography { shootAll() },

            { shooterPreset } to choreography { flywheel?.let { spinUpShooter(it.preset) } ?: freeze() },

            { rezeroTurret } to choreography { turret?.rezero(electrical) ?: freeze() },
            { reindexCarousel } to choreography {
                carousel.whereAreMyBalls()
                rumble.set(TwoSided(0.Percent, 100.Percent))
            },

            { !turretManual.isZero } to choreography {
                scope.launch { withTimeout(5.Second) { flashlight?.set(On) } }
                turret?.manualOverride(operator) ?: freeze()
            },

            { ball0 } to {
                carousel.state.clear()
            },
            { ball1 } to {
                carousel.state.clear()
                carousel.state.push(1)
            },
            { ball2 } to {
                carousel.state.clear()
                carousel.state.push(2)
            },
            { ball3 } to {
                carousel.state.clear()
                carousel.state.push(3)
            },
        )
    }
}

suspend fun Subsystems.intakeBalls() = startChoreo("Intake Balls") {
    choreography {
        while (isActive) {
            val angle = carousel.state.intakeAngle()
            if (angle == null) {
                log(WARN) { "I'm full. No open slots in carousel magazine." }

                launch { intakeSlider?.set(IntakeSliderState.In) }
                launch { intakeRollers?.set(0.Percent) }
                rumble.set(TwoSided(100.Percent, 0.Percent))
            } else {
                launch { feederRoller?.set(0.Rpm) }
                carousel.set(angle)
                launch { carousel.set(angle, 0.Degree) }

                launch { intakeSlider?.set(IntakeSliderState.Out) }
                launch { intakeRollers?.optimalEat(drivetrain, electrical) }

                log(INFO) { "Waiting for a yummy mouthful of balls." }

                carousel.delayUntilBall()
                carousel.state.push()
            }
        }
    }
}

suspend fun Subsystems.visionAimTurret() {
    if (turret == null) {
        log(ERROR) { "Need turret for vision" }
        freeze()
    } else startChoreo("Vision Aim Turret")
    {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret.hardware.position.readEagerly().withoutStamps

        choreography {
            log(DEBUG) { "target ${(turretPos - reading!!.tx).Degree}" }
            turret.set(
                turretPos - reading!!.tx
            )
            freeze()
        }

    }

}

suspend fun Subsystems.visionAim() {
    if (flywheel == null || feederRoller == null || turret == null) {
        log(ERROR) { "Need flywheel and feederRoller for vision aiming" }
        freeze()
    } else startChoreo("Vision Flywheel") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val turretPosition by turret.hardware.position.readEagerly().withoutStamps
        val pitch by drivetrain.hardware.pitch.readEagerly().withoutStamps

        choreography {
            scope.launch { withTimeout(7.Second) { flashlight?.set(On) } }
            launch { turret.fieldOrientedAngle(drivetrain) }

            val reading1 = reading
            if (reading1?.pipeline == null) {
                log(ERROR) { "Limelight reading1 == $reading1" }
                return@choreography
            }

            launch { limelight.set(reading1.pipeline) }

            val snapshot1 = bestShot(
                limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing, pitch)
            )
            if (snapshot1 == null) {
                log(WARN) { "Couldn't find snapshot1 or no shots possible" }
                coroutineContext[Job]!!.cancelChildren()
                return@choreography
            }

            launch {
                turret.fieldOrientedAngle(
                    drivetrain,
                    turretPosition - reading1.tx + limelight.hardware.conversions.mountingBearing
                )
            }

            withTimeout(1.Second) { limelight.autoZoom() }

            val reading2 = reading
            if (reading2?.pipeline == null) {
                log(ERROR) { "Limelight reading2 == $reading2" }
                return@choreography
            }

            launch { limelight.set(reading2.pipeline) }

            val snapshot2 = bestShot(
                limelight.hardware.conversions.goalPositions(reading2, robotPosition.bearing, pitch)
            )
            if (snapshot2 == null) {
                log(ERROR) { "Couldn't find snapshot2 or no shots possible" }
                coroutineContext[Job]!!.cancelChildren()
                return@choreography
            }

            launch {
                turret.fieldOrientedAngle(
                    drivetrain,
                    turretPosition - reading2.tx + limelight.hardware.conversions.mountingBearing
                )
            }
            spinUpShooter(snapshot2.flywheel, snapshot2.hood)
        }
    }
}

suspend fun Subsystems.shootOne() = startChoreo("Shoot One") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps
    choreography {
        val angle = carousel.state.shootAngle()
        if (angle == null) {
            log(ERROR) { "I feel empty. I want to eat some balls." }
            withTimeout(2.Second) { rumble.set(TwoSided(100.Percent, 0.Percent)) }
        } else {
            launch {
                carousel.set(angle, 0.CarouselSlot)
            }

            log(INFO) { "Waiting for ball to launch." }
            withTimeout(1.5.Second) {
                flywheel?.delayUntilBall()
            } ?: log(ERROR) { "Did not detect ball launch. Assuming slot was actually empty." }
            carousel.state.pop()

            coroutineContext[Job]!!.cancelChildren()
            delay(100.milli(Second)) // Prevent accidentally shooting twice
        }
    }
}

suspend fun Subsystems.shootAll() = startChoreo("Shoot All") {
    choreography {
        launch { carousel.set(carousel.fireAllDutycycle) }
        while (isActive) {
            flywheel?.delayUntilBall()
            log(DEBUG) { "Ball shot" }
            carousel.state.pop()
        }
    }
}

suspend fun Subsystems.spinUpShooter(flywheelTarget: AngularVelocity, hoodTarget: ShooterHoodState? = null) {
    if (flywheel == null || feederRoller == null) {
        log(ERROR) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("Spin Up Shooter") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { feederRoller.set(0.Rpm) }

            carousel.state.shootInitialAngle()?.let { angle ->
                carousel.set(angle)

                launch { flywheel.set(flywheelTarget) }
                launch { feederRoller.set(feederRoller.feedSpeed) }

                delayUntilFeederAndFlywheel(flywheelTarget)

                log(INFO) { "Feeder roller and flywheel set" }
                hoodTarget?.let {
                    launch { shooterHood?.set(it) }
                }

                runWhenever({
                    feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
                            && flywheelSpeed in flywheelTarget `±` flywheel.tolerance
                } to choreography {
                    scope.launch {
                        withTimeout(.5.Second) {
                            rumble.set(TwoSided(0.Percent, 100.Percent))
                        }
                    }
                    freeze()
                })
            }
        }
    }
}
