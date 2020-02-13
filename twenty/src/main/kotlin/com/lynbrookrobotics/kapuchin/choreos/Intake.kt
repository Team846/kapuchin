package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakeSliderState.*
import info.kunalsheth.units.generated.*
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

suspend fun Subsystems.intakeBalls() = if (carousel != null)
    startChoreo("Intake Balls") {

        val magazine by carousel.hardware.magazine.readEagerly().withoutStamps

        choreography {
            val isMagazineFull = magazine.all { it }
            launch { intakeSlider?.set(Out) }
            delay(500.Millisecond)
            launch { intakeRollers?.set(intakeRollers.intakeSpeed) }
            runWhenever(
                    { !isMagazineFull } to choreography { carousel.spinToCollectPosition() },
                    { isMagazineFull } to choreography { rumble.set(TwoSided(50.Percent, 50.Percent)) }
            )
            freeze()
        }
    } else Unit

suspend fun Subsystems.unjamBalls() = startChoreo("Unjam Balls") {

    choreography {
        launch { intakeSlider?.set(Out) }
        launch { intakeRollers?.set(intakeRollers.unjamSpeed) }
        freeze()
    }
}