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


class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, OffloadedOutput>(hardware) {
    override val fallbackController: TurretComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent) }


    override fun TurretHardware.output(value: OffloadedOutput) {
        val aLL = atLeftLimit.optimizedRead(currentTime, 0.Second)
        val aLR = atRightLimit.optimizedRead(currentTime, 0.Second)
        when {
            aLL.y || aLR.y -> value.writeTo(0.0)
            else -> value.writeTo(turretEsc)
        }
    }
}

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period: Time
        get() = 50.Millisecond
    override val syncThreshold: Time
        get() = 20.Millisecond
    override val priority: Priority
        get() = High
    override val name: String
        get() = "Turret"

    private val turretEscId by pref(16)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val turretEsc by hardw { CANSparkMax(turretEscId, CANSparkMaxLowLevel.MotorType.kBrushless) }

    val leftLimitSwitch by hardw { turretMotorEsc.getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }
    val rightLimitSwitch by hardw { turretMotorEsc.getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }

    val atLeftLimit = sensor(leftLimitSwitch) { get() stampWith it }
    val atRightLimit = sensor(rightLimitSwitch) { get() stampWith it }
}

