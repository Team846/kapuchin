package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.twenty.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.twenty.subsystems.climber.*
import com.lynbrookrobotics.twenty.subsystems.driver.OperatorHardware
import com.revrobotics.CANSparkMax.SoftLimitDirection.kForward
import info.kunalsheth.units.generated.*

suspend fun ClimberWinchComponent.set(target: DutyCycle, slowMode: Boolean) = startRoutine("Set") {
    controller {
        if(slowMode) ClimberWinchOutput.RunningNoSafety(PercentOutput(hardware.escConfig, target))
        else ClimberWinchOutput.Running(PercentOutput(hardware.escConfig, target))
    }
}

suspend fun ClimberPivotComponent.set(target: ClimberPivotState) = startRoutine("Set") {
    controller { target }
}

