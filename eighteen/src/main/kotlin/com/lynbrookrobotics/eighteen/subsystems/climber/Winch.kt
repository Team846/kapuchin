package com.lynbrookrobotics.eighteen.subsystems.climber

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class WinchComponent(hardware: WinchHardware) : Component<WinchComponent, WinchHardware, V>(hardware, EventLoop) {

    val climbStrength by pref(10, Volt)

    override val fallbackController: WinchComponent.(Time) -> V = { 0.Volt }

    override fun WinchHardware.output(value: V) = lazyOutput(PercentOutput(
            value / hardware.operatingVoltage
    ))
}

class WinchHardware : SubsystemHardware<WinchHardware, WinchComponent>() {
    override val priority get() = TODO()
    override val period = 100.milli(Second)
    override val syncThreshold = 50.milli(Second)
    override val name = "Climber Winch"

    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(30, Ampere)

    val middleEscId by pref(6)
    val maxForward by pref(100, Percent)
    val maxReverse by pref(10, Percent)
    val middleEsc by hardw { TalonSRX(middleEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit)
        it.configPeakOutputForward(maxForward.siValue, 100)
        it.configPeakOutputReverse(-maxReverse.siValue, 100)
    }
    val lazyOutput = lazyOutput(middleEsc)

    val leftEscId by pref(5)
    val leftEsc by hardw { TalonSRX(leftEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.configPeakOutputReverse(0.0, 100)
        it.follow(middleEsc)
    }

    val rightEscId by pref(7)
    val rightEsc by hardw { TalonSRX(rightEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.configPeakOutputReverse(0.0, 100)
        it.follow(middleEsc)
    }
}