package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakeSliderState.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

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
                 /*ball in slot and empty slots available --> spin to empty slot*/
                 /*ball in slot but everythings full --> ???*/
        )
        freeze()
    }
}

suspend fun Subsystems.unjamBalls() = startChoreo("Unjam Balls") {

    choreography {
        launch { intakeSlider?.set(Out) }
        launch { intakeRollers?.set(intakeRollers.unjamSpeed) }
        freeze()
    }
}