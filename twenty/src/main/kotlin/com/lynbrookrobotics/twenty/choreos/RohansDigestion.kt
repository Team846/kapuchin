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
import com.lynbrookrobotics.twenty.subsystems.shooter.FlashlightState
import com.lynbrookrobotics.twenty.subsystems.shooter.ShooterHoodState
import info.kunalsheth.units.generated.*
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

suspend fun Subsystems.visionAimTurretSnapshot() {
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
//        val pitch by drivetrain.hardware.pitch.readEagerly().withoutStamps
        val shift by operator.shift.readEagerly().withoutStamps

        choreography {
            launch { feederRoller.set(0.Rpm) }

            carousel.rezero()
            carousel.set(carousel.state.shootInitialAngle() ?: carousel.hardware.nearestSlot())

            launch { feederRoller.set(feederRoller.feedSpeed) }
            launch { leds?.blink(Color.BLUE) }

            var flywheelSetpoint = flywheelPreset

            launch { flywheel.set(flywheelPreset) }
            launch {
                while (isActive) {
                    if (!shift) {
                        val snapshot = reading?.copy()
                        if (snapshot != null) {
//                            val distance = limelight.hardware.conversions.distanceToGoal(snapshot, pitch)
                            var distance = limelight.hardware.conversions.distanceToGoal(snapshot, 0.Degree)

                            if (distance > flywheel.innerPortDistanceThreshold) {
                                log(Debug) { "Distance: ${distance.Foot} ft, aiming for outer"}
                                val delta = 0.Foot // TODO sid
                                distance -= delta
                            }
                            // TODO sid
                            // if skew > tolerance, aim for outer
//                            println("SHOOTING ${
//                                listOf(distance.Foot,
//                                    snapshot.ty.Degree,
//                                    pitch.Degree,
//                                    snapshot.pipeline?.number ?: -1).joinToString()
//                            }")

                            val target = flywheel.hardware.conversions.rpmCurve(distance)

                            if ((target - flywheelPreset).abs > 2000.Rpm) {
                                log(Error) { "Calculated target (${target.Rpm} rpm) differs greatly from preset (${flywheelPreset.Rpm} rpm)" }
                            } else {
                                flywheelSetpoint = target
                                launch { flywheel.set(target) }
                            }
                        }
                    }
                }
            }

            runWhenever({ flywheelSpeed in flywheelSetpoint `±` flywheel.tolerance } to {
                launch { leds?.set(Color.GREEN) }
                rumble.set(100.Percent)
            })
        }
    }
}