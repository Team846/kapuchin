package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*


suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {
    val intakeUp by operator.intakeUp.readEagerly().withoutStamps
    val intakeDown by operator.intakeDown.readEagerly().withoutStamps
    val collect by operator.collect.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { intakeUp } to choreography { setIntakeUp() },
                { intakeDown } to choreography { setIntakeDown() },
                { collect } to choreography { collect() }
        )
    }
}

suspend fun Subsystems.setIntakeUp() = supervisorScope {
    var intakeUp: Job? = null
    try {
        intakeUp = launch { intakePivot?.set(IntakePivotState.Up) }
        freeze()
    } finally {
        withContext(NonCancellable) {
            intakeUp?.cancel()
        }
    }
}

suspend fun Subsystems.setIntakeDown() = supervisorScope {
    var intakeDown: Job? = null
    try {
        launch { intakePivot?.set(IntakePivotState.Down) }
        freeze()
    } finally {
        withContext(NonCancellable) {
            intakeDown?.cancel()

        }
    }
}

suspend fun Subsystems.collect() = supervisorScope() {
    try {
        launch { intakeRollers?.spin(intakeRollers.collectSpeed) }
        launch { carousel?.spin(carousel.carouselSpeed) }
        freeze()
    } finally {
        withContext(NonCancellable) {
            withTimeout(1.Second) {
                launch { carousel?.spin(50.Percent) }
            }
        }
    }
}
