package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import info.kunalsheth.units.generated.*

suspend fun CollectorRollersComponent.spin(electrical: ElectricalSystemHardware, Rollers: V)=startRoutine("spin"){
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
                voltageToDutyCycle(Rollers, vBat)
    }
}
suspend fun CollectorRollersComponent.set(target:DutyCycle)=startRoutine("set"){
    controller {target}
}
