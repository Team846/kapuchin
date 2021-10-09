package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.lynbrookrobotics.twenty.subsystems.intake.IntakeSliderState
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.digestionTeleop() = startChoreo("Digestion Teleop") {

    val shift by operator.shift.readEagerly().withoutStamps

    val eatBalls by driver.eatBalls.readEagerly().withoutStamps
    val pukeBallsIntakeIn by driver.pukeBallsIntakeIn.readEagerly().withoutStamps
    val pukeBallsIntakeOut by driver.pukeBallsIntakeOut.readEagerly().withoutStamps

    val aim by operator.aim.readEagerly().withoutStamps
    val shoot by operator.shoot.readEagerly().withoutStamps

    val shooterPresetAnitez by operator.shooterPresetAnitez.readEagerly().withoutStamps
    val shooterPresetLow by operator.shooterPresetLow.readEagerly().withoutStamps
    val shooterPresetMed by operator.shooterPresetMed.readEagerly().withoutStamps
    val shooterPresetHigh by operator.shooterPresetHigh.readEagerly().withoutStamps

    val carouselBall0 by operator.carouselBall0.readEagerly().withoutStamps
    val centerTurret by operator.centerTurret.readEagerly().withoutStamps
    val reindexCarousel by operator.reindexCarousel.readEagerly().withoutStamps

    val turretManual by operator.turretManual.readEagerly().withoutStamps
    val turretPrecisionManual by operator.turretPrecisionManual.readEagerly().withoutStamps

    val carouselLeft by driver.carouselLeft.readEagerly().withoutStamps
    val carouselRight by driver.carouselRight.readEagerly().withoutStamps

    choreography {
        withTimeout(2.Second) { carousel.rezero() }

        if (carousel.indexOnStart) {
            withTimeout(15.Second) {
                log(Debug) { "Reindexing carousel" }
                whereAreMyBalls()
            }
        }

        runWhenever(
            { eatBalls } to { intakeBalls() },
            { pukeBallsIntakeIn } to { intakeRollers?.set(-100.Percent) ?: freeze() },
            { pukeBallsIntakeOut } to {
                launch { intakeSlider?.set(IntakeSliderState.Out) }
                intakeRollers?.set(-100.Percent) ?: freeze()
            },

            { aim } to { visionAimTurret() },
            { shoot } to { shootAll(hoodDown = shift) },

            { shooterPresetAnitez } to { flywheel?.let { spinUpShooter(it.presetAnitez) } ?: freeze() },
            { shooterPresetLow } to { flywheel?.let { spinUpShooter(it.presetLow) } ?: freeze() },
            { shooterPresetMed } to { flywheel?.let { spinUpShooter(it.presetMed) } ?: freeze() },
            { shooterPresetHigh } to { flywheel?.let { spinUpShooter(it.presetHigh) } ?: freeze() },

            { carouselBall0 } to { carousel.state.clear() },
            { centerTurret } to { turret?.set(0.Degree) ?: freeze() },
            { reindexCarousel } to { whereAreMyBalls() },

            { !turretManual.isZero && turretPrecisionManual.isZero } to {
                scope.launch { withTimeout(3.Second) { flashlight?.set(FlashlightState.On) } }
                turret?.manualOverride(operator) ?: freeze()
            },
            { turretManual.isZero && !turretPrecisionManual.isZero } to {
                scope.launch { withTimeout(3.Second) { flashlight?.set(FlashlightState.On) } }
                turret?.manualPrecisionOverride(operator) ?: freeze()
            },

            { carouselLeft } to { carousel.set(carousel.hardware.nearestSlot() + 1.CarouselSlot, 0.Degree) },
            { carouselRight } to { carousel.set(carousel.hardware.nearestSlot() - 1.CarouselSlot, 0.Degree) }
        )
    }
}

suspend fun Subsystems.digestionTest() = startChoreo("Digestion Test") {

    val turretManual by operator.turretManual.readEagerly().withoutStamps

    choreography {
        runWhenever(
            // carousel
            { operator.xbox.pov == 270 } to {
                carousel.set(carousel.hardware.nearestSlot() + 1.CarouselSlot,
                    0.Degree)
            },
            { operator.xbox.pov == 90 } to { carousel.set(carousel.hardware.nearestSlot() - 1.CarouselSlot, 0.Degree) },

            // intake
            { operator.lb } to { intakeSlider?.set(IntakeSliderState.Out) },
            { operator.lt } to { intakeRollers?.set(intakeRollers.eatSpeed) },
            { operator.rt } to { intakeRollers?.set(-100.Percent) },

            // flywheel
            { operator.xbox.xButton } to { flywheel?.set(flywheel.presetAnitez) },

            // turret
            { !turretManual.isZero } to { turret?.manualOverride(operator) },
            { operator.xbox.pov == 0 } to { turret?.set(0.Degree, 0.Degree) },

            // feeder roller
            { operator.xbox.aButton } to { feederRoller?.set(feederRoller.feedSpeed) },

            // flashlight
            { operator.xbox.bButton } to { flashlight?.set(FlashlightState.On) },
        )
    }
}

suspend fun Subsystems.intakeBalls() = startChoreo("Intake Balls") {
    choreography {
        while (isActive) {
            val angle = carousel.state.intakeAngle()
            if (angle == null) {
                log(Warning) { "I'm full. No open slots in carousel magazine." }

                launch { intakeSlider?.set(IntakeSliderState.In) }
                launch { intakeRollers?.set(0.Percent) }
                rumble.set(TwoSided(100.Percent, 0.Percent))
            } else {
                launch { feederRoller?.set(0.Rpm) }
                launch { intakeRollers?.set(0.Percent) }

                carousel.set(angle)
                launch { carousel.set(angle, 0.Degree) }

                launch { intakeSlider?.set(IntakeSliderState.Out) }
                launch { intakeRollers?.set(intakeRollers.eatSpeed) }

                log(Debug) { "Waiting for a yummy mouthful of balls." }

                carousel.delayUntilBall()
                carousel.state.push()
            }
        }
    }
}

suspend fun Subsystems.visionAimTurret() {
    if (turret == null) {
        log(Error) { "Need turret for vision" }
        freeze()
    } else startChoreo("Vision Aim Turret") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val turretPos by turret.hardware.position.readEagerly().withoutStamps

        choreography {
            reading?.let { snapshot ->
                launch {
                    turret.set(
                        turretPos - snapshot.tx + limelight.hardware.conversions.mountingBearing,
                        0.Degree
                    )
                }
            }

            runWhenever(
                {
                    reading?.tx?.let { tx -> (tx + limelight.hardware.conversions.mountingBearing).abs < 2.Degree }
                        ?: false
                } to {
                    flashlight?.strobe()
                }
            )
        }
    }
}

suspend fun Subsystems.shootAll(hoodDown: Boolean) = startChoreo("Shoot All") {
    choreography {
        try {
            if (!hoodDown) {
                launch { shooterHood?.set(ShooterHoodState.Up) }
                delay(0.3.Second)
            }
            carousel.set(carousel.fireAllDutycycle)
        } finally {
            withContext(NonCancellable) {
                carousel.state.clear()
                carousel.rezero()
                carousel.hardware.encoder.position = 0.0
            }
        }
    }
}

suspend fun Subsystems.spinUpShooter(flywheelTarget: AngularVelocity, hoodTarget: ShooterHoodState? = null) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("Spin Up Shooter") {
        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { flashlight?.set(FlashlightState.On) }
            launch { feederRoller.set(0.Rpm) }

            carousel.rezero()
            carousel.set(carousel.state.shootInitialAngle() ?: carousel.hardware.nearestSlot())

            launch { flywheel.set(flywheelTarget) }
            launch { feederRoller.set(feederRoller.feedSpeed) }

            delayUntilFeederAndFlywheel(flywheelTarget)

            log(Debug) { "Feeder roller and flywheel set" }
            hoodTarget?.let {
                launch { shooterHood?.set(it) }
            }

            runWhenever({
                feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
                        && flywheelSpeed in flywheelTarget `±` flywheel.tolerance
            } to {
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

suspend fun CarouselComponent.delayUntilBall() = startChoreo("Delay Until Ball") {
    val color by hardware.color.readEagerly().withoutStamps
    val proximity by hardware.proximity.readEagerly().withoutStamps

    choreography {
        delayUntil {
            hardware.conversions.detectingBall(proximity, color)
        }
    }
}

suspend fun FlywheelComponent.delayUntilBall() = startChoreo("Delay Until Ball") {
    val speed by hardware.speed.readEagerly().withStamps
    val dvdt = differentiator(::p, speed.x, speed.y)

    val initialSpeed = speed.y

    var lastAcceleration = 0.Rpm / Second
    var lastPercentSpeed = 0.Percent

    choreography {
        delayUntil(clock) {
            val acceleration = dvdt(speed.x, speed.y)
            val percentSpeed = (speed.y - initialSpeed) / initialSpeed

            val accelerating = percentSpeed > lastPercentSpeed
            if (acceleration > lastAcceleration) log(Debug) {
                "Peak deceleration: ${lastAcceleration.RpmPerSecond withDecimals 2} RPM/sec"
            }

            if (percentSpeed > lastPercentSpeed) log(Debug) {
                "Peak percent drop: ${lastPercentSpeed.Percent withDecimals 1}%"
            }

            (accelerating &&
                    lastAcceleration < hardware.conversions.ballDecelerationThreshold &&
                    lastPercentSpeed < hardware.conversions.ballPercentDropThreshold)

                .also {
                    lastAcceleration = acceleration
                    lastPercentSpeed = percentSpeed
                }
        }
    }
}

suspend fun Subsystems.delayUntilFeederAndFlywheel(
    flywheelTarget: AngularVelocity,
) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to run." }
    } else startChoreo("Delay until feeder and flywheel") {

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
//            log(Debug) { "Waiting for feeder roller to get up to speed" }
//            delayUntil {
//                feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
//            }
//            log(Debug) { "${feederSpeed.Rpm} | ${feederRoller.feedSpeed.Rpm}" }
//            log(Debug) { "Feeder roller set" }
//
//            log(Debug) { "Waiting for flywheel to get up to speed" }
//            delayUntil {
//                flywheelSpeed in flywheelTarget `±` flywheel.tolerance
//            }
//            log(Debug) { "${flywheelSpeed.Rpm} | ${flywheelTarget.Rpm}" }
//            log(Debug) { "Flywheel set" }
            delay(2.Second)
        }
    }
}

suspend fun Subsystems.whereAreMyBalls() = startChoreo("Re-Index") {
    val color by carousel.hardware.color.readEagerly().withoutStamps
    val proximity by carousel.hardware.proximity.readEagerly().withoutStamps

    choreography {
        carousel.rezero()
        var slotsSkipped = 0
        carousel.state.clear()
        for (i in 0 until carousel.state.maxBalls) {
            carousel.set(i.CarouselSlot)
            val j = launch { carousel.set(i.CarouselSlot, 0.Degree) }
            delay(0.1.Second)
            if (carousel.hardware.conversions.detectingBall(proximity, color)) {
                carousel.state.push(slotsSkipped + 1)
                slotsSkipped = 0
            } else {
                slotsSkipped++
            }
            j.cancel()
        }
        rumble.set(TwoSided(0.Percent, 100.Percent))
    }
}