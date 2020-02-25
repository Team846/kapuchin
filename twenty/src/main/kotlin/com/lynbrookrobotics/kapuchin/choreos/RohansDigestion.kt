package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.FlashlightState.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun Subsystems.digestionTeleop() = startChoreo("Digestion Teleop") {

    val intakeBalls by driver.intakeBalls.readEagerly().withoutStamps
    val unjamBalls by driver.unjamBalls.readEagerly().withoutStamps

    val aim by operator.aim.readEagerly().withoutStamps
    val aimPreset by operator.aimPreset.readEagerly().withoutStamps
    val shoot by operator.shoot.readEagerly().withoutStamps
    val hoodUp by operator.hoodUp.readEagerly().withoutStamps

    val flywheelManual by operator.flywheelManual.readEagerly().withoutStamps
    val turretManual by operator.turretManual.readEagerly().withoutStamps

    val rezeroTurret by operator.rezeroTurret.readEagerly().withoutStamps
    val reindexCarousel by operator.reindexCarousel.readEagerly().withoutStamps
    val centerTurret by operator.centerTurret.readEagerly().withoutStamps

    choreography {
        if (turret != null && !turret.hardware.isZeroed) launch {
            turret.rezero(electrical)
        }

        carousel.rezero()
        carousel.whereAreMyBalls()

        launch {
            launchWhenever(
//                    { turret?.routine == null } to choreography { turret?.fieldOrientedPosition(drivetrain) },
                    { shoot } to choreography {
                        fire()
                        delay(250.milli(Second))
                    }
            )
        }
        runWhenever(
                { intakeBalls } to choreography { eat() },
                { unjamBalls } to choreography { intakeRollers?.set(intakeRollers.pukeSpeed) ?: freeze() },

                { aim } to choreography { visionAim() },
                { aimPreset } to choreography {
                    flywheel?.let { spinUpShooter(flywheel.preset, Down) } ?: freeze()
                },
                { hoodUp } to choreography { shooterHood?.set(Up) ?: freeze() },

                { flywheelManual != null } to choreography {
                    flywheel?.let {
                        spinUpShooter(
                                (flywheelManual ?: 0.Percent) * it.maxSpeed,
                                if (hoodUp) Up else Down
                        )
                    } ?: freeze()
                    // TODO verify this is controllable -andy
                },
                { !turretManual.isZero } to choreography {
                    launch { flashlight?.set(On) }
                    turret?.manualOverride(operator) ?: freeze()
                },

                { rezeroTurret } to choreography { turret?.rezero(electrical) ?: freeze() },
                { reindexCarousel } to choreography {
                    carousel.rezero()
                    carousel.whereAreMyBalls()
                    rumble.set(TwoSided(0.Percent, 100.Percent))
                },
                { centerTurret } to choreography { turret?.set(0.Degree) }
        )
    }

}

suspend fun Subsystems.eat() = startChoreo("Collect Balls") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        carousel.rezero()
        while (isActive) {
            val emptySlot = carousel.state.closestEmpty(carouselAngle + carousel.collectSlot)

            if (emptySlot == null) {
                log(Warning) { "I'm full. No open slots in carousel magazine." }
                rumble.set(TwoSided(100.Percent, 0.Percent))
            } else {
                launch { feederRoller?.set(0.Rpm) }
                carousel.set(emptySlot - carousel.collectSlot)
                launch { carousel.set(emptySlot - carousel.collectSlot, 0.Degree) }

                launch { intakeSlider?.set(IntakeSliderState.Out) }
                launch { intakeRollers?.set(intakeRollers.eatSpeed) }

                log(Debug) { "Waiting for a yummy mouthful of balls." }
                carousel.delayUntilBall()
                carousel.state.set(carouselAngle + carousel.collectSlot, true)
            }
        }
    }
}

suspend fun Subsystems.visionAim() {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feederRoller for vision aiming" }
        freeze()
    } else startChoreo("Vision Aim") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps

        choreography {
            val reading1 = reading
            if (reading1?.pipeline == null) {
                log(Error) { "Limelight pipeline is null!!" }
                return@choreography
            }

            launch { limelight.set(reading1.pipeline) }
            val snapshot1 = bestShot(limelight.hardware.conversions.goalPositions(reading1, robotPosition.bearing))
            if (snapshot1 == null) {
                log(Debug) { "Couldn't find snapshot1 or no shots possible" }
                withTimeout(.5.Second) { flashlight?.strobe() }
                return@choreography
            }

            withTimeout(2.Second) {
                turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, 1.Degree)
            }
            launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot1.goal, null) }

            withTimeout(1.Second) { limelight.autoZoom() }

            val snapshot2 = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
            if (snapshot2 == null) {
                log(Warning) { "Couldn't find snapshot2 - no reading or no best shot" }
                withTimeout(.5.Second) { flashlight?.strobe() }
                return@choreography
            }

            launch { turret?.trackTarget(limelight, flywheel, drivetrain, snapshot2.goal) }
            spinUpShooter(snapshot2.flywheel, snapshot2.hood)
        }
    }
}

suspend fun Subsystems.fire() = startChoreo("Fire") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)

        if (fullSlot == null) {
            log(Warning) { "I feel empty. I want to eat some balls." }
            withTimeout(2.Second) {
                launch { rumble.set(TwoSided(100.Percent, 0.Percent)) }
                launch { flashlight?.strobe() }
            }
        } else {
            val j = launch { carousel.set(fullSlot - carousel.shootSlot, 0.CarouselSlot) }

            log(Debug) { "Waiting for ball to launch." }
            withTimeout(3.Second) {
                flywheel?.delayUntilBall()
            } ?: log(Error) { "Did not detect ball launch. Assuming slot was actually empty." }
            carousel.state.set(carouselAngle + carousel.shootSlot, false)

            j.cancel()
        }
    }
}

private suspend fun Subsystems.spinUpShooter(flywheelTarget: AngularVelocity, hoodTarget: ShooterHoodState) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("Spin Up Shooter") {

        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { flashlight?.set(On) }

            launch { feederRoller.set(0.Rpm) }
            val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
            if (fullSlot != null) {
                val target = fullSlot - carousel.shootSlot
                if (target > carouselAngle) carousel.set(target - 0.5.CarouselSlot)
                if (target < carouselAngle) carousel.set(target + 0.5.CarouselSlot)
            }

            launch { flywheel.set(flywheelTarget) }
            launch { feederRoller.set(feederRoller.feedSpeed) }

            fun feederCheck() = feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
            fun flywheelCheck() = flywheelSpeed in flywheelTarget `±` flywheel.tolerance

            log(Debug) { "Waiting for feeder roller to get up to speed" }
            delayUntil(f = ::feederCheck)

            log(Debug) { "Waiting for flywheel to get up to speed" }
            delayUntil(f = ::flywheelCheck)

            launch { shooterHood?.set(hoodTarget) }

            runWhenever({
                feederCheck() && flywheelCheck()
            } to choreography {
                rumble.set(TwoSided(0.Percent, 100.Percent))
            })
        }
    }
}