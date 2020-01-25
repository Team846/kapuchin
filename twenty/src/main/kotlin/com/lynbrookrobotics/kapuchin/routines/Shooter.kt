package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import info.kunalsheth.units.generated.*

suspend fun FeederRollerComponent.spin(electrical: ElectricalSystemHardware, Rollers: V) = startRoutine("spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        voltageToDutyCycle(Rollers, vBat)
    }
}

suspend fun FeederRollerComponent.set(target: DutyCycle) = startRoutine("set") {
    controller { target }
}

suspend fun ShooterComponent.spin(electrical: ElectricalSystemHardware, left: V, right: V) = startRoutine("Spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        TwoSided(
                voltageToDutyCycle(left, vBat),
                voltageToDutyCycle(right, vBat)
        )

    }
}

suspend fun ShooterComponent.set(state: TwoSided<DutyCycle>) = startRoutine("Set") {
    controller { state }
}


suspend fun TurretComponent.spin(electrical: ElectricalSystemHardware, turret: V) = startRoutine("Spin") {
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {

        voltageToDutyCycle(turret, vBat)


    }
}

suspend fun TurretComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { target }
}
