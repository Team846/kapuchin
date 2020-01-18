package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import info.kunalsheth.units.generated.*

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period: Time
        get() = 50.Millisecond
    override val syncThreshold: Time
        get() = 20.Millisecond
    override val priority: Priority
        get() = High
    override val name: String
        get() = "Turret"

    private val turretMotorId by pref(16)

    val turretMotor by hardw { CANSparkMax(turretMotorId, CANSparkMaxLowLevel.MotorType.kBrushless) }
}

class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, DutyCycle>(hardware) {
    override val fallbackController: TurretComponent.(Time) -> DutyCycle
        get() = { 0.Percent }

    override fun TurretHardware.output(value: DutyCycle) {
        turretMotor.set(value.Each)
    }
}
