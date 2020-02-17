package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.CarouselMagazineState.Companion.collectSlot
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

suspend fun Subsystems.eat(init: CarouselMagazineState): CarouselMagazineState = coroutineScope {
    var state = init

    val target = init.closestOpenSlot(collectSlot)

    if (target == null) {
        log(Warning) { "I'm full. No open slots in carousel magazine." }
        rumble.set(TwoSided(100.Percent))
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

        log(Debug) { "Waiting for a yummy mouthful of balls." }
        carousel?.delayUntilBall()
        state.set(collectSlot, true)
    }
}

suspend fun Subsystems.puke() = coroutineScope {
    intakeRollers?.set(intakeRollers.pukeSpeed)
}

suspend fun Subsystems.aim() = limelight?.let {
    startChoreo("Aim") {
        val readings by limelight.hardware.readings.readEagerly().withoutStamps
        choreography {
        }
    }
}