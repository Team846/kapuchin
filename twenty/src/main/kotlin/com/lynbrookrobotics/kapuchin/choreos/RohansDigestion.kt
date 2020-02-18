package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.CarouselMagazineState.Companion.collectSlot
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

suspend fun Subsystems.eat(init: CarouselMagazineState): CarouselMagazineState = coroutineScope {
    var state = init

    val target = init.closestOpenSlot()

    if (target == null) {
        log(Warning) { "I'm full. No open slots in carousel magazine." }
        rumble.set(TwoSided(100.Percent, 0.Percent))
        init
    } else {
        repeat(abs(target)) {
            if (target > 0) state = state.rotateCW(false)
            if (target < 0) state = state.rotateCCW(false)
        }
        carousel?.set(target.CarouselSlot, state, 10.Degree)
        launch { carousel?.set(target.CarouselSlot, state, 0.Degree) }

        launch { intakeSlider?.set(IntakeSliderState.Out) }
        launch { intakeRollers?.set(intakeRollers.eatSpeed) }

        log(Debug) { "Waiting for a yummy mouthfuls of balls." }
        carousel?.delayUntilBall()
        state.set(collectSlot, true)
    }
}

suspend fun Subsystems.puke() = coroutineScope {
    intakeRollers?.set(intakeRollers.pukeSpeed)
}

suspend fun Subsystems.adjustForOptimalFart() {
    if (limelight != null && flywheel != null && feederRoller != null) startChoreo("Aim") {
        val readings by limelight.hardware.readings.readEagerly().withoutStamps
        val robotPosition by drivetrain.hardware.position.readEagerly().withoutStamps

        val flywheelSpeed by flywheel.hardware.speed.readEagerly().withoutStamps
        val feederSpeed by feederRoller.hardware.speed.readEagerly().withoutStamps

        choreography {
            launch { turret?.trackTarget(limelight) }
            readings?.let { snapshot ->
                bestShot(limelight.hardware.conversions.goalPositions(snapshot, robotPosition.bearing))
            }?.let { shot ->
                launch { flywheel.set(shot.flywheel) }
                launch { feederRoller.set(feederRoller.feedSpeed) }

                val feederCheck: () -> Boolean = {
                    feederSpeed in feederRoller.feedSpeed `±` feederRoller.tolerance
                }
                val flywheelCheck: () -> Boolean = {
                    flywheelSpeed in shot.flywheel `±` flywheel.tolerance
                }

                log(Debug) { "Waiting for feeder roller to get up to speed" }
                delayUntil(f = feederCheck)

                log(Debug) { "Waiting for flywheel to get up to speed" }
                delayUntil(f = flywheelCheck)

                runWhenever({
                    feederCheck() && flywheelCheck()
                } to {
                    rumble.set(TwoSided(0.Percent, 100.Percent))
                })
            }
        }
    } else log(Error) { "Need limelight, flywheel, and feeder to aim" }
}

suspend fun Subsystems.accidentallyShart(init: CarouselMagazineState): CarouselMagazineState = coroutineScope {
    var state = init

    val target = init.closestClosedSlot()

    if (target == null) {
        log(Warning) { "I feel empty. I want to eat some balls." }
        rumble.set(TwoSided(100.Percent, 0.Percent))
        init
    } else {
        var targetState = state
        repeat(abs(target) - 1) {
            if (target > 0) targetState = targetState.rotateCW(false)
            if (target < 0) targetState = targetState.rotateCCW(false)
        }
        if (target > 0) targetState = targetState.rotateCW(true)
        if (target < 0) targetState = targetState.rotateCCW(true)

        launch { carousel?.set(target.CarouselSlot, targetState) }

        log(Debug) { "Waiting for fart to crown." }
        withTimeout(3.Second) {
            flywheel?.delayUntilBall()
            state.set(collectSlot, true)
        }

        targetState
    }
}