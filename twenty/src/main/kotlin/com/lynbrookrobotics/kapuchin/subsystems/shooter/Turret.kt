package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANDigitalInput
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

    val turretMotorEsc by hardw { CANSparkMax(turretMotorId, CANSparkMaxLowLevel.MotorType.kBrushless) }

    val leftLimitSwitch by hardw { turretMotorEsc.getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }
    val rightLimitSwitch by hardw { turretMotorEsc.getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }

    val atLeftLimit = sensor(leftLimitSwitch) { get() stampWith it }
    val atRightLimit = sensor(rightLimitSwitch) { get() stampWith it }
}

class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, DutyCycle>(hardware) {
    override val fallbackController: TurretComponent.(Time) -> DutyCycle
        get() = { 0.Percent }

    override fun TurretHardware.output(value: DutyCycle) {
        val aLL = atLeftLimit.optimizedRead(currentTime, 0.Second)
        val aLR = atRightLimit.optimizedRead(currentTime, 0.Second)
        when {
            aLL.y || aLR.y -> turretMotorEsc.set(0.0)
            else -> turretMotorEsc.set(value.Each)
        }
    }
}
