package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberPivotState
import info.kunalsheth.units.generated.*

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {
    val toggleClimberArms by operator.toggleClimberArms.readEagerly().withoutStamps
    val chaChaRealSmooth by operator.chaChaRealSmooth.readEagerly().withoutStamps
    val takeItBackNowYall by operator.takeItBackNowYall.readEagerly().withoutStamps

    var climberArms =
        if (climberPivot?.hardware?.pivotSolenoid?.get() == ClimberPivotState.Up.output) ClimberPivotState.Up else ClimberPivotState.Down

    choreography {
        runWhenever(
            { climberArms == ClimberPivotState.Down } to { climberPivot?.set(ClimberPivotState.Down) },
            { chaChaRealSmooth } to { climberWinch?.set(climberWinch.extendSpeed) },
            { takeItBackNowYall } to { climberWinch?.set(climberWinch.retractSpeed) },

            { toggleClimberArms } to {
                delay(1.Second) // prevent accidental taps
                climberArms = if (climberArms == ClimberPivotState.Up) ClimberPivotState.Down else ClimberPivotState.Up
                freeze()
            },
        )
    }
}
