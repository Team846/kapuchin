package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.generated.milli

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(::Second)
    override val syncThreshold = 1.milli(::Second)
    override val subsystemName = "Drivetrain"

    val leftSlaveEscId by pref(14)
    val rightSlaveEscId by pref(13)
    val rightMasterEscId by pref(11)
    val leftMasterEscId by pref(12)

    val escCanTimeout by pref(0.001::Second)


    val operatingVoltage by pref(11::Volt)
    val currentLimit by pref(20::Ampere)


    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit)
    }
    val leftSlaveEsc by hardw { TalonSRX(leftSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(leftMasterEsc)
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc, escCanTimeout)


    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit)
    }
    val rightSlaveEsc by hardw { TalonSRX(rightSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(rightMasterEsc)
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc, escCanTimeout)
}