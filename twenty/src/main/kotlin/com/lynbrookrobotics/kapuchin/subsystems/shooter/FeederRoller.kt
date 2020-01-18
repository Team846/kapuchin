package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import info.kunalsheth.units.generated.*

class FeederRollerHardware : SubsystemHardware<FeederRollerHardware, FeederRollerComponent>() {
    override val period: Time
        get() = 50.Millisecond
    override val syncThreshold: Time
        get() = 20.Millisecond
    override val priority: Priority
        get() = Medium
    override val name: String
        get() = "Feeder Roller"

    private val feederRollerMotorId by pref(55)

    val feederRollerMotor by hardw { CANSparkMax(feederRollerMotorId, CANSparkMaxLowLevel.MotorType.kBrushless) }
}

class FeederRollerComponent(hardware: FeederRollerHardware) : Component<FeederRollerComponent, FeederRollerHardware, DutyCycle>(hardware) {
    override val fallbackController: FeederRollerComponent.(Time) -> DutyCycle
        get() = { 0.Percent }

    override fun FeederRollerHardware.output(value: DutyCycle) {
        feederRollerMotor.set(value.Each)
    }
}