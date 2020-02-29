package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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

suspend fun Subsystems.extendClimber() = coroutineScope {
    turret?.set(turret.windupPosition, 5.Degree)

    scope.launch { climberPivot?.set(Up) }
    launch { climberWinch?.set(climberWinch.extendSpeed) }

    freeze()
}

suspend fun Subsystems.retractClimber() = coroutineScope {
    if (climberPivot == null) {
        log(Error) { "Climber pivot is required to retract." }
        return@coroutineScope
    }

    turret?.set(turret.windupPosition, 5.Degree)

    launch { climberWinch?.set(-climberWinch.retractSpeed) }
    delay(climberPivot.retractDelay)
    scope.launch { climberPivot.set(Down) }

    freeze()
}
