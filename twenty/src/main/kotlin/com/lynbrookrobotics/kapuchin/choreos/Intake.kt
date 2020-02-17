package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakeSliderState.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake Teleop") {

    val intakeBalls by driver.intakeBalls.readEagerly().withoutStamps
    val unjamBalls by driver.unjamBalls.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { intakeBalls } to choreography { intakeBalls() },
                { unjamBalls } to choreography { unjamBalls() }
        )
    }
}

suspend fun Subsystems.intakeBalls() = startChoreo("Intake Balls") {

    choreography {
        launch { intakeSlider?.set(Out) }
        launch { intakeRollers?.set(intakeRollers.intakeSpeed) }
        runWhenever(
                { TODO("full slot") } to choreography { TODO("Spin to full slot") }
        )
        freeze()
    }
}

suspend fun Subsystems.unjamBalls() = coroutineScope {
    launch { intakeSlider?.set(Out) }
    launch { intakeRollers?.set(intakeRollers.unjamSpeed) }
    freeze()
}