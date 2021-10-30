package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import com.lynbrookrobotics.twenty.subsystems.intake.IntakeSliderState
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import java.awt.Color

suspend fun Subsystems.digestionTeleop() = startChoreo("Digestion Teleop") {

    val shift by operator.shift.readEagerly().withoutStamps

    val eatBalls by driver.eatBalls.readEagerly().withoutStamps
    val pukeBallsIntakeIn by driver.pukeBallsIntakeIn.readEagerly().withoutStamps
    val pukeBallsIntakeOut by driver.pukeBallsIntakeOut.readEagerly().withoutStamps

    val centerTurret by operator.centerTurret.readEagerly().withoutStamps
    val aim by operator.aim.readEagerly().withoutStamps
    val shootFast by operator.shootFast.readEagerly().withoutStamps
    val shootSlow by operator.shootSlow.readEagerly().withoutStamps

    val presetAnitez by operator.presetAnitez.readEagerly().withoutStamps
    val presetClose by operator.presetClose.readEagerly().withoutStamps
    val presetMed by operator.presetMed.readEagerly().withoutStamps
    val presetFar by operator.presetFar.readEagerly().withoutStamps

    val turretManual by operator.turretManual.readEagerly().withoutStamps
    val turretPrecisionManual by operator.turretPrecisionManual.readEagerly().withoutStamps

    val carouselBall0 by driver.carouselBall0.readEagerly().withoutStamps
    val carouselLeft by driver.carouselLeft.readEagerly().withoutStamps
    val carouselRight by driver.carouselRight.readEagerly().withoutStamps

    val increaseFlywheelSpeed by driver.increaseFlywheelSpeed.readEagerly().withoutStamps
    val decreaseFlywheelSpeed by driver.decreaseFlywheelSpeed.readEagerly().withoutStamps

    choreography {
        if (!carousel.hardware.isZeroed) {
            withTimeout(2.Second) { carousel.rezero() }
        }

        runWhenever(
            { eatBalls } to { intakeBalls() },
            { pukeBallsIntakeIn } to { intakeRollers?.set(-100.Percent) ?: freeze() },
            { pukeBallsIntakeOut } to {
                launch { intakeSlider?.set(IntakeSliderState.Out) }
                intakeRollers?.set(-100.Percent) ?: freeze()
            },

            { centerTurret } to { turret?.set(0.Degree) ?: freeze() },
            { aim && !shift } to { visionTrackTarget() },
            { aim && shift } to { flashlight?.set(FlashlightState.On) },
            { shootFast } to { shootAll(carousel.shootFastSpeed) },
            { shootSlow } to { shootAll(carousel.shootSlowSpeed) },

            { presetAnitez } to { flywheel?.let { spinUpShooter(it.presetAnitez) } ?: freeze() },
            { presetClose } to { flywheel?.let { spinUpShooter(it.presetClose) } ?: freeze() },
            { presetMed } to { flywheel?.let { spinUpShooter(it.presetMed) } ?: freeze() },
            { presetFar } to { flywheel?.let { spinUpShooter(it.presetFar) } ?: freeze() },

            { !turretManual.isZero && turretPrecisionManual.isZero } to {
                scope.launch { withTimeout(3.Second) { flashlight?.set(FlashlightState.On) } }
                turret?.manualOverride(operator) ?: freeze()
            },
            { turretManual.isZero && !turretPrecisionManual.isZero } to {
                scope.launch { withTimeout(3.Second) { flashlight?.set(FlashlightState.On) } }
                turret?.manualPrecisionOverride(operator) ?: freeze()
            },

            { carouselBall0 } to { carousel.state.clear() },
            { carouselLeft && !eatBalls } to {
                carousel.set(carousel.hardware.nearestSlot() + 1.CarouselSlot,
                    0.Degree)
            },
            { carouselRight && !eatBalls } to {
                carousel.set(carousel.hardware.nearestSlot() - 1.CarouselSlot,
                    0.Degree)
            },

            { increaseFlywheelSpeed } to {
                flywheel?.changeFlywheelSpeed(true)
                delay(1.Second)
            },
            { decreaseFlywheelSpeed } to {
                flywheel?.changeFlywheelSpeed(false)
                delay(1.Second)
            }
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
    val isBall by carousel.hardware.isBall.readEagerly().withoutStamps
    val carouselLeft by driver.carouselLeft.readEagerly().withoutStamps
    val carouselRight by driver.carouselRight.readEagerly().withoutStamps

    choreography {
        while (isActive) {
            val angle = carousel.state.intakeAngle()
            if (angle == null) {
                launch { leds?.blink(Color.RED) }
                log(Warning) { "I'm full. No open slots in carousel magazine." }

                launch { intakeSlider?.set(IntakeSliderState.In) }
                launch { intakeRollers?.set(0.Percent) }
                freeze()
            } else {
                launch { feederRoller?.set(0.Rpm) }
                launch { intakeRollers?.set(intakeRollers.pauseSpeed) }

                carousel.set(angle)
                launch { carousel.set(angle, 0.Degree) }
                launch { leds?.blink(Color.BLUE) }

                launch { intakeSlider?.set(IntakeSliderState.Out) }
                launch { intakeRollers?.set(intakeRollers.eatSpeed) }

                log(Debug) { "Waiting for a yummy mouthful of balls." }

                delayUntil { isBall || carouselLeft || carouselRight }
                launch { leds?.set(Color.RED) }
                carousel.state.push()
            }
        }
    }
}

suspend fun Subsystems.visionTrackTarget() = startChoreo("Vision Aim Turret") {
    choreography {
        launch { flashlight?.set(FlashlightState.On) }
        turret?.trackTarget(drivetrain, limelight)
    }
}

suspend fun Subsystems.shootAll(speed: DutyCycle) = startChoreo("Shoot All") {
    choreography {
        val j = launch { shooterHood?.set(ShooterHoodState.Up) }
        launch { leds?.set(Color.GREEN) }
        try {
            delay(0.3.Second)
            carousel.set(speed)
        } finally {
            withContext(NonCancellable) {
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
}

suspend fun Subsystems.spinUpShooter(flywheelPreset: AngularVelocity) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("Spin Up Shooter") {
        val reading by limelight.hardware.readings.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val shift by operator.shift.readEagerly().withoutStamps

        choreography {
            launch { feederRoller.set(0.Rpm) }

            carousel.rezero()
            carousel.set(carousel.state.shootInitialAngle() ?: carousel.hardware.nearestSlot())

            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { leds?.blink(Color.BLUE) }

            var flywheelTarget = flywheelPreset

            launch { flywheel.set(flywheelPreset) }
            launch {
                while (isActive) {
                    if (!shift) {
                        val snapshot = reading?.copy()
                        if (snapshot != null) {
                            val target = targetFlywheelSpeed(flywheel, snapshot)
                            if (target !in 5000.Rpm `±` 2000.Rpm) {
                                log(Error) { "Calculated target (${target.Rpm} rpm) is too far off" }
                            } else {
                                flywheelTarget = target
                                log(Debug) { "New target ${target.Rpm} * ${flywheel.rpmPercentage.Percent}%" }
                                launch { flywheel.set(target * flywheel.rpmPercentage) }
                            }
                        }
                    }
                    delay(200.milli(Second)) // TODO idk how long
                }
            }

            runWhenever({ flywheelSpeed in flywheelTarget `±` flywheel.tolerance } to {
                launch { leds?.set(Color.GREEN) }
                rumble.set(100.Percent)
            })
        }
    }
}