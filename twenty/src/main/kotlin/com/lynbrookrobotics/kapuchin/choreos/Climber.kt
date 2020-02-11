package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberChodeState.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {

    val extendClimber by operator.extendClimber.readEagerly().withoutStamps
    val retractClimber by operator.retractClimber.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { extendClimber } to choreography { extendClimber() },
                { retractClimber } to choreography { retractClimber() }
        )
    }
}

suspend fun Subsystems.extendClimber() = startChoreo("Extend Climber") {

    choreography {
        launch { climberChode?.set(Off) }
        scope.launch { climberPivot?.set(Up) }
        launch { climberWinch?.set(climberWinch.extendSpeed) }
        freeze()
    }
}

suspend fun Subsystems.retractClimber() = startChoreo("Retract Climber") {

    choreography {
        launch { climberChode?.set(Off) }
        scope.launch { climberPivot?.set(Down) }
        launch { climberWinch?.set(climberWinch.retractSpeed) }
        freeze()
    }
}
