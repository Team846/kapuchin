package com.lynbrookrobotics.twenty.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.routines.set
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberBrakeState
import com.lynbrookrobotics.twenty.subsystems.climber.ClimberPivotState
import info.kunalsheth.units.generated.*

suspend fun Subsystems.climberTeleop() = startChoreo("Climber Teleop") {

    val shift by operator.shift.readEagerly().withoutStamps

    val toggleClimberArms by operator.toggleClimberArms.readEagerly().withoutStamps
    val chaChaRealSmooth by operator.chaChaRealSmooth.readEagerly().withoutStamps
    val takeItBackNowYall by operator.takeItBackNowYall.readEagerly().withoutStamps

    var climberArms =
        if (climberPivot?.hardware?.pivotSolenoid?.get() == ClimberPivotState.Up.output) ClimberPivotState.Up else ClimberPivotState.Down

    choreography {
        runWhenever(
            { climberArms == ClimberPivotState.Down } to { climberPivot?.set(ClimberPivotState.Down) },
            { chaChaRealSmooth } to { climberWinch?.set(if (shift) climberWinch.extendSlowSpeed else climberWinch.extendSpeed) },
            { takeItBackNowYall } to { climberWinch?.set(if (shift) climberWinch.retractSlowSpeed else climberWinch.retractSpeed) },

            { toggleClimberArms } to {
                delay(1.Second) // prevent accidental taps
                climberArms = if (climberArms == ClimberPivotState.Up) ClimberPivotState.Down else ClimberPivotState.Up
                freeze()
            },
        )
    }
}

suspend fun Subsystems.climberTest() = startChoreo("Climber Test") {

    var climberArms =
        if (climberPivot?.hardware?.pivotSolenoid?.get() == ClimberPivotState.Up.output) ClimberPivotState.Up else ClimberPivotState.Down

    choreography {
        runWhenever(
            { climberArms == ClimberPivotState.Down } to { climberPivot?.set(ClimberPivotState.Down) },

            // arms
            { operator.rb } to {
                delay(1.Second) // prevent accidental taps
                climberArms = if (climberArms == ClimberPivotState.Up) ClimberPivotState.Down else ClimberPivotState.Up
                freeze()
            },

            // brake
            { operator.xbox.yButton } to { climberWinch?.hardware?.brakeSolenoid?.set(ClimberBrakeState.Off.output) },

            // winch
            { operator.xbox.backButton && !operator.xbox.startButton } to {
                climberWinch?.hardware?.masterEsc?.set(climberWinch.extendSlowSpeed.Each)
            },
            { operator.xbox.backButton && operator.xbox.startButton } to {
                climberWinch?.hardware?.masterEsc?.set(climberWinch.retractSlowSpeed.Each)
            },
        )
    }
}