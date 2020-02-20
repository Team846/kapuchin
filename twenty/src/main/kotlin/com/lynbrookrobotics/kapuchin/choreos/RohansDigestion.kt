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
import com.lynbrookrobotics.kapuchin.subsystems.shooter.ShooterHoodState.*
import edu.wpi.first.wpilibj.Relay.Value.kOn
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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

    choreography {
        // In case turret wasn't zeroed during autonomous
        if (turret?.hardware?.isZeroed == true) launch { turret.rezero(electrical) }

        launch {
            launchWhenever(
                    { turret?.routine == null } to choreography { turret?.fieldOrientedPosition(drivetrain) },
                    { limelight?.routine == null } to choreography { limelight?.autoZoom() }
            )
        }

        runWhenever(
                { intakeBalls } to choreography { eat() },
                { unjamBalls } to choreography { intakeRollers?.set(intakeRollers.pukeSpeed) ?: freeze() },

                { aim } to choreography { visionAim() },
                { aimPreset } to choreography { flywheel?.let { generalAim(flywheel.preset, Down) } ?: freeze() },
                { shoot } to choreography { accidentallyShart() },
                { hoodUp } to choreography { shooterHood?.set(Up) ?: freeze() },

                { !flywheelManual.isZero } to choreography { flywheel?.manualOverride(operator) ?: freeze() },
                { !turretManual.isZero } to choreography {
                    launch { flashlight?.set(kOn) }
                    turret?.manualOverride(operator) ?: freeze()
                },

                { rezeroTurret } to choreography { turret?.rezero(electrical) ?: freeze() },
                { reindexCarousel } to choreography { carousel.whereAreMyBalls() }
        )
    }

}

suspend fun Subsystems.eat() = startChoreo("Intake Balls") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        val emptySlot = carousel.state.closestEmpty(carouselAngle + carousel.collectSlot)

        if (emptySlot == null) {
            log(Warning) { "I'm full. No open slots in carousel magazine." }
            rumble.set(TwoSided(100.Percent, 0.Percent))
        } else {
            launch { feederRoller?.set(0.Rpm) }
            carousel.set(emptySlot - carousel.collectSlot, 0.1.CarouselSlot)
            launch { carousel.set(emptySlot - carousel.collectSlot, 0.Degree) }

            launch { intakeSlider?.set(IntakeSliderState.Out) }
            launch { intakeRollers?.set(intakeRollers.eatSpeed) }

            log(Debug) { "Waiting for a yummy mouthfuls of balls." }
            carousel.delayUntilBall()
            carousel.state.set(carouselAngle + carousel.collectSlot, true)
        }
    }
}

suspend fun Subsystems.visionAim() {
    if (limelight == null || flywheel == null || feederRoller == null) {
        log(Error) { "Need limelight, flywheel, and feederRoller for vision aiming" }
        freeze()
    } else startChoreo("Aim") {

        val reading by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps

        choreography {
            val shot = reading?.let { bestShot(limelight.hardware.conversions.goalPositions(it, robotPosition.bearing)) }
            if (shot == null) withTimeout(.5.Second) {
                log(Debug) { "Cannot find target or no shots possible" }
                rumble.error()
            } else {
                launch { turret?.trackTarget(limelight, flywheel, drivetrain, shot.goal) }
                generalAim(shot.flywheel, shot.hood)
            }
        }
    }
}

suspend fun Subsystems.accidentallyShart() = startChoreo("Shoot") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)

        if (fullSlot == null) {
            log(Warning) { "I feel empty. I want to eat some balls." }
            launch {
                withTimeout(2.Second) { flashlight?.strobe() }
            }
            rumble.set(TwoSided(100.Percent, 0.Percent))
        } else {
            launch { carousel.set(fullSlot - carousel.shootSlot, 0.CarouselSlot) }

            log(Debug) { "Waiting for ball to launch." }
            withTimeout(3.Second) {
                flywheel?.delayUntilBall()
            } ?: log(Error) { "Did not detect ball launch. Assuming slot was actually empty." }
            carousel.state.set(carouselAngle + carousel.shootSlot, false)
        }
    }
}

private suspend fun Subsystems.generalAim(flywheelTarget: AngularVelocity, hoodTarget: ShooterHoodState) {
    if (flywheel == null || feederRoller == null) {
        log(Error) { "Need flywheel and feeder to spin up shooter" }
        freeze()
    } else startChoreo("General Aim") {

        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { flashlight?.set(kOn) }

            launch {
                val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
                if (fullSlot != null) supervisorScope {
                    val target = fullSlot - carousel.shootSlot
                    if (target > carouselAngle) carousel.set(target - 1.CarouselSlot)
                    if (target < carouselAngle) carousel.set(target + 1.CarouselSlot)
                }
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