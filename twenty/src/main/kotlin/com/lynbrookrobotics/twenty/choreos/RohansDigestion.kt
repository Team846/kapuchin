package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
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

    choreography {
        if (turret != null && !turret.hardware.isZeroed) launch {
            log(Debug) { "Rezeroing turret" }
            turret.rezero(electrical)
        }

//        withTimeout(15.Second) {
//            log(Debug) { "Reindexing carousel" }
//            carousel.rezero()
//            carousel.whereAreMyBalls()
//        }

        launch {
            launchWhenever(
//                { turret?.routine == null } to choreography { turret?.trackPositionFieldOriented(drivetrain, UomVector(0.Foot, -26.Foot)) },
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
        )
    }

}

suspend fun Subsystems.intakeBalls() = startChoreo("Intake Balls") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
//        carousel.rezero()
        while (isActive) {
            val emptySlot = carousel.state.closestEmpty(carouselAngle + carousel.collectSlot)

            if (emptySlot == null) {
                log(Warning) { "I'm full. No open slots in carousel magazine." }

                launch { intakeSlider?.set(IntakeSliderState.In) }
                launch { intakeRollers?.set(0.Percent) }
                rumble.set(TwoSided(100.Percent, 0.Percent))
            } else {
                launch { feederRoller?.set(0.Rpm) }
                carousel.set(emptySlot - carousel.collectSlot)
                launch { carousel.set(emptySlot - carousel.collectSlot, 0.Degree) }

                launch { intakeSlider?.set(IntakeSliderState.Out) }
                launch { intakeRollers?.optimalEat(drivetrain, electrical) }

                log(Debug) { "Waiting for a yummy mouthful of balls." }
                carousel.delayUntilBall()
                carousel.state.set(carouselAngle + carousel.collectSlot, true)
            }
        }
    }
}

suspend fun Subsystems.visionAimTurret() {
    if (turret == null) {
        log(Error) { "Need turret for vision" }
        freeze()
    } else startChoreo("Vision Aim Turret")
    {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret.hardware.position.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps
        val pitch by drivetrain.hardware.pitch.readEagerly().withoutStamps

        choreography {
            val reading1 = reading?.copy()
            if (reading1?.pipeline == null) {
                log(Error) { "Limelight reading1 == $reading" }
            } else {
                launch { limelight.set(reading1.pipeline) }
                launch {
                    turret.set(turretPos - reading1.tx)
                }
//                limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing, pitch)
                withTimeout(1.Second) { limelight.autoZoom() }
            }
        }

    }

}

suspend fun Subsystems.visionAim() {
    if (flywheel == null || feederRoller == null || turret == null) {
        log(Error) { "Need flywheel and feederRoller for vision aiming" }
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
                log(Error) { "Limelight reading1 == $reading1" }
                return@choreography
            }

            launch { limelight.set(reading1.pipeline) }

            val snapshot1 = bestShot(
                limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing, pitch)
            )
            if (snapshot1 == null) {
                log(Warning) { "Couldn't find snapshot1 or no shots possible" }
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
                log(Error) { "Limelight reading2 == $reading2" }
                return@choreography
            }

            launch { limelight.set(reading2.pipeline) }

            val snapshot2 = bestShot(
                limelight.hardware.conversions.goalPositions(reading2, robotPosition.bearing, pitch)
            )
            if (snapshot2 == null) {
                log(Error) { "Couldn't find snapshot2 or no shots possible" }
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
        val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
        val nextSlot = carouselAngle.roundToInt(CarouselSlot).CarouselSlot

        if (fullSlot == null) {
            log(Warning) { "I feel empty. I want to eat some balls." }
            withTimeout(2.Second) { rumble.set(TwoSided(100.Percent, 0.Percent)) }
        }

        launch {
            carousel.set((fullSlot ?: nextSlot) - carousel.shootSlot, 0.CarouselSlot)
        }

        log(Debug) { "Waiting for ball to launch." }
        withTimeout(1.5.Second) {
            flywheel?.delayUntilBall()
        } ?: log(Error) { "Did not detect ball launch. Assuming slot was actually empty." }
        carousel.state.set(carouselAngle + carousel.shootSlot, false)

        coroutineContext[Job]!!.cancelChildren()
        delay(100.milli(Second)) // Prevent accidentally shooting twice
    }
}

suspend fun Subsystems.shootAll() = startChoreo("Shoot All") {
    choreography {
        launch { carousel.set(carousel.fireAllDutycycle) }
        while (isActive) {
            flywheel?.delayUntilBall()
            log(Debug) { "Ball shot" }
//            carousel.state.pop()
        }
    }
}

suspend fun Subsystems.spinUpShooter(flywheelTarget: AngularVelocity, hoodTarget: ShooterHoodState? = null) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("Spin Up Shooter") {



        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { feederRoller.set(0.Rpm) }
//
//            val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
//            if (fullSlot != null) {
//                val target = fullSlot - carousel.shootSlot
//                if (target > carouselAngle) carousel.set(target - 0.5.CarouselSlot)
//                if (target < carouselAngle) carousel.set(target + 0.5.CarouselSlot)
//            }

            println("ASDF ${flywheelTarget.Rpm}")
            launch { flywheel.set(flywheelTarget) }
//            launch { feederRoller.set(feederRoller.feedSpeed) }
//
//            fun feederCheck() = feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
//            fun flywheelCheck() = flywheelSpeed in flywheelTarget `±` flywheel.tolerance
//
//            log(Debug) { "Waiting for feeder roller to get up to speed" }
//            withTimeout(5.Second) {
//                delayUntil(predicate = ::feederCheck)
//            } ?: log(Error) {
//                "Feeder roller never got up to speed (target = ${
//                    feederRoller.feedSpeed.Rpm withDecimals 0
//                } RPM, current = ${
//                    feederSpeed.Rpm withDecimals 0
//                })"
//            }
//
//            log(Debug) { "Waiting for flywheel to get up to speed" }
//            withTimeout(5.Second) {
//                delayUntil(predicate = ::flywheelCheck)
//            } ?: log(Error) {
//                "Flywheel never got up to speed (target = ${
//                    flywheelTarget.Rpm withDecimals 0
//                } RPM, current = ${
//                    flywheelSpeed.Rpm withDecimals 0
//                })"
//            }
//
//            log(Debug) { "Feeder roller and flywheel set" }
//            hoodTarget?.let {
//                launch { shooterHood?.set(it) }
//            }
//
//            runWhenever({
//                feederCheck() && flywheelCheck()
//            } to choreography {
//                scope.launch {
//                    withTimeout(.5.Second) {
//                        rumble.set(TwoSided(0.Percent, 100.Percent))
//                    }
//                }
//                freeze()
//            })
        }
    }
}