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
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
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
        launch { turret?.rezero(electrical) }
        launch {
            launchWhenever({ turret?.routine == null } to choreography { turret?.fieldOrientedPosition(drivetrain) })
        }
        runWhenever(
                { intakeBalls } to choreography { eat() },
                { unjamBalls } to choreography { puke() },

                { aim } to choreography { adjustForOptimalFart() },
                { aimPreset } to choreography { println("unimplemented") },
                { shoot } to choreography { accidentallyShart() },
                { hoodUp } to choreography { shooterHood?.set(Up) },

                { !flywheelManual.isZero } to choreography { flywheel?.manualOverride(operator) },
                { !turretManual.isZero } to choreography { turret?.manualOverride(operator) },

                { rezeroTurret } to choreography { turret?.rezero(electrical) },
                { reindexCarousel } to choreography { carousel.whereAreMyBalls() }
        )
    }

}

suspend fun Subsystems.eat() = startChoreo("Collect") {
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

suspend fun Subsystems.puke() = coroutineScope {
    intakeRollers?.set(intakeRollers.pukeSpeed)
}

suspend fun Subsystems.adjustForOptimalFart() {
    if (limelight != null && flywheel != null && feederRoller != null) startChoreo("Aim") {

        val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

        val readings by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { turret?.trackTarget(limelight) }

            launch {
                val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)
                if (fullSlot != null) supervisorScope {
                    val target = fullSlot - carousel.shootSlot
                    if (target > carouselAngle) carousel.set(target - 1.CarouselSlot)
                    if (target < carouselAngle) carousel.set(target + 1.CarouselSlot)
                }
            }

            readings?.let { snapshot ->
                bestShot(limelight.hardware.conversions.goalPositions(snapshot, robotPosition.bearing))
            }?.let { shot ->
                launch { flywheel.set(shot.flywheel) }
                launch { feederRoller.set(feederRoller.feedSpeed) }

                fun feederCheck() = feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
                fun flywheelCheck() = flywheelSpeed in shot.flywheel `±` flywheel.tolerance

                log(Debug) { "Waiting for feeder roller to get up to speed" }
                delayUntil(f = ::feederCheck)

                log(Debug) { "Waiting for flywheel to get up to speed" }
                delayUntil(f = ::flywheelCheck)

                launch { shooterHood?.set(shot.hood) }

                runWhenever({
                    feederCheck() && flywheelCheck()
                } to choreography {
                    rumble.set(TwoSided(0.Percent, 100.Percent))
                })
            }
        }
    } else log(Error) { "Need limelight, flywheel, and feeder to aim" }
}

suspend fun Subsystems.targetZoneAim() = couroutineScope {
    launch { flywheel?.set(flywheel.target) }
}

suspend fun Subsystems.accidentallyShart() = startChoreo("Shoot") {
    val carouselAngle by carousel.hardware.position.readEagerly().withoutStamps

    choreography {
        val fullSlot = carousel.state.closestFull(carouselAngle + carousel.shootSlot)

        if (fullSlot == null) {
            log(Warning) { "I feel empty. I want to eat some balls." }
            rumble.set(TwoSided(100.Percent, 0.Percent))
        } else {
            launch { carousel.set(fullSlot - carousel.shootSlot, 0.CarouselSlot) }

            log(Debug) { "Waiting for fart to crown." }
            withTimeout(3.Second) {
                flywheel?.delayUntilBall()
                carousel.state.set(carouselAngle + carousel.shootSlot, true)
            }
        }
    }
}