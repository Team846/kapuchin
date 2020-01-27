package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import info.kunalsheth.units.generated.*

suspend fun CollectorRollersComponent.spin(electrical: ElectricalSystemHardware, Rollers: V) = startRoutine("spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        voltageToDutyCycle(Rollers, vBat)
    }
}

suspend fun CollectorRollersComponent.set(target: DutyCycle) = startRoutine("set") {
    controller { target }
}

suspend fun CarouselComponent.spin(electrical: ElectricalSystemHardware, Storage: V) = startRoutine("spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        voltageToDutyCycle(Storage, vBat)
    }
}

suspend fun IntakePivotComponent.set(target: IntakePivotState) = startRoutine("Set") {
    controller { target }
}
