package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*

class WinchComponent(hardware: WinchHardware) : Component<WinchComponent, WinchHardware, Volt>(hardware) {

    val climbStrength by pref(10, `To Volt`)

    override val fallbackController: WinchComponent.(Time) -> Volt = { 0.Volt }

    override fun WinchHardware.output(value: Volt) = lazyOutput(PercentOutput(
            value / hardware.operatingVoltage
    ))
}

class WinchHardware : SubsystemHardware<WinchHardware, WinchComponent>() {
    override val priority = Priority.Low
    override val period = 100.milli(::Second)
    override val syncThreshold = 50.milli(::Second)
    override val subsystemName = "Climber Winch"

    val operatingVoltage by pref(11, `To Volt`)
    val currentLimit by pref(30, `To Ampere`)

    val middleEscId by pref(16)
    val maxForward by pref(100, `To Percent`)
    val maxReverse by pref(10, `To Percent`)
    val middleEsc by hardw { TalonSRX(middleEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, period)
        it.configPeakOutputForward(maxForward.siValue, 100)
        it.configPeakOutputReverse(-maxReverse.siValue, 100)
    }
    val lazyOutput = lazyOutput(middleEsc, syncThreshold)

    val leftEscId by pref(15)
    val leftEsc by hardw { TalonSRX(leftEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit, period)
        it.configPeakOutputReverse(0.0, 100)
        it.follow(middleEsc)
    }

    val rightEscId by pref(17)
    val rightEsc by hardw { TalonSRX(rightEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit, period)
        it.configPeakOutputReverse(0.0, 100)
        it.follow(middleEsc)
    }
}