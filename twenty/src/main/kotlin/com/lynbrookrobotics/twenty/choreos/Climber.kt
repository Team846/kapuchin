package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberPivotState.Down
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberPivotState.Up
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {

    val extendClimber by operator.extendClimber.readEagerly().withoutStamps
    val retractClimber by operator.retractClimber.readEagerly().withoutStamps

    choreography {
        runWhenever(
            { extendClimber } to choreography { extendClimber() },
            { retractClimber } to choreography { retractClimber()  },
        )
    }
}

suspend fun Subsystems.extendClimber() = coroutineScope {
    turret?.set(turret.windupPosition, 5.Degree)

    scope.launch { climberPivot?.set(Up) }
    delay(1.Second)
    launch { climberWinch?.set(climberWinch.extendSpeed) }

    freeze()
}

suspend fun Subsystems.retractClimber() = coroutineScope {
    turret?.set(turret.windupPosition, 5.Degree)

    scope.launch { climberPivot?.set(Down) }
    delay(1.Second)
    launch { climberWinch?.set(-climberWinch.retractSpeed) }

    freeze()
}