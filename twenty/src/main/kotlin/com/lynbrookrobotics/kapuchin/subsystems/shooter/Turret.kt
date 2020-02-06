package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANDigitalInput
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyClosed
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import info.kunalsheth.units.generated.*


class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, OffloadedOutput>(hardware) {
    override val fallbackController: TurretComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }


    override fun TurretHardware.output(value: OffloadedOutput) {
        val atLeft = leftLimit.optimizedRead(currentTime, 0.Second).y
        val atRight = rightLimit.optimizedRead(currentTime, 0.Second).y
        when {
            atLeft || atRight -> turretEsc.set(0.0)
            else -> value.writeTo(turretEsc, turretPidController)
        }
    }
}

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period: Time = 50.Millisecond
    override val syncThreshold: Time = 20.Millisecond
    override val priority: Priority = High
    override val name: String = "Turret"

    private val turretEscId by pref(16)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val turretEsc by hardw { CANSparkMax(turretEscId, CANSparkMaxLowLevel.MotorType.kBrushless) }

    val turretPidController: CANPIDController by hardw { turretEsc.pidController }

    private val switchNormallyOpen by pref(true)

    private val limitSwitchPolarity = if (switchNormallyOpen) {
        kNormallyOpen
    } else {
        kNormallyClosed
    }
    val leftStop: CANDigitalInput by hardw { turretEsc.getForwardLimitSwitch(limitSwitchPolarity) }.configure {
        +it.enableLimitSwitch(true)
    }
    val rightStop: CANDigitalInput by hardw { turretEsc.getReverseLimitSwitch(limitSwitchPolarity) }.configure {
        +it.enableLimitSwitch(true)
    }

    val leftLimit = sensor(leftStop) { get() stampWith it }
    val rightLimit = sensor(rightStop) { get() stampWith it }
}
