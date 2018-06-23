package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.TwoSided
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.dsl.hardw
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val wheelDiameter by pref(6::Inch)

    val encoderToWheelGears by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        ({ GearTrain(encoderGear, wheelGear) })
    }

    val offloadedSettings by pref {
        val nativeOutputUnits by pref(1023::Tick)
        val perOutputQuantity by pref(12::Volt)
        val nativeFeedbackUnits by pref(4096::Tick)
        val perFeedbackQuantity by pref(1::Turn)
        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = nativeOutputUnits, perOutputQuantity = perOutputQuantity, nativeFeedbackUnits = nativeFeedbackUnits,
                    perFeedbackQuantity = wheelDiameter * PI * encoderToWheelGears.inputToOutput(perFeedbackQuantity).Turn
            )
        })
    }

    val velocityGains by pref {
        val kP by pref(12::Volt, 3::FootPerSecond)
        val kI by pref(4::Volt, 1::Foot)
        val kD by pref(0::Volt, 1::FootPerSecondSquared)
        ({ PidGains(kP, kI, kD) })
    }

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        VelocityOutput(
                offloadedSettings.native(velocityGains), offloadedSettings.native(0.FootPerSecond)
        ).let { TwoSided(it, it) }
    }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)
    }
}

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


    val voltageCompensation by pref(11::Volt)
    val currentLimit by pref(20::Ampere)


    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        configMaster(it, voltageCompensation, currentLimit)
    }
    val leftSlaveEsc by hardw { TalonSRX(leftSlaveEscId) }.configure {
        configSlave(it, voltageCompensation, currentLimit)
        it.follow(leftMasterEsc)
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc, escCanTimeout)


    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, voltageCompensation, currentLimit)
    }
    val rightSlaveEsc by hardw { TalonSRX(rightSlaveEscId) }.configure {
        configSlave(it, voltageCompensation, currentLimit)
        it.follow(rightMasterEsc)
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc, escCanTimeout)
}