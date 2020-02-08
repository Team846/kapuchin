package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import info.kunalsheth.units.generated.*

suspend fun ClimberWinchComponent.spin(target: OffloadedOutput) = startRoutine("Spin") {
    controller { target }
}

suspend fun ClimberPivotComponent.set(state: ClimberPivotState) = startRoutine("Set") {
    controller { state }
}