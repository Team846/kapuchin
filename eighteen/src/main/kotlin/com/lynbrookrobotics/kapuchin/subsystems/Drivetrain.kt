package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware, driver: DriverHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

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

    val maxLeftSpeed by pref(13::FootPerSecond)
    val maxRightSpeed by pref(13.3::FootPerSecond)
    val topSpeed get() = maxLeftSpeed min maxRightSpeed
    val velocityGains by pref {
        val kP by pref(12::Volt, 3::FootPerSecond)
        val kI by pref(4::Volt, 1::Foot)
        val kD by pref(0::Volt, 1::FootPerSecondSquared)
        ({ PidGains(kP, kI, kD) })
    }
    val leftVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxLeftSpeed))
    val rightVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxRightSpeed))

    val trackSize by pref(2::Foot)
    val maxTurningSpeed get() = topSpeed / (trackSize / 2)
    val turningVelocityGains by pref {
        val kP by pref(6::FootPerSecond, 45::DegreePerSecond)
        val kI by pref(2::FootPerSecond, 45::Degree)
        val kD by pref(0::FootPerSecond, 45::DegreePerSecondSquared)
        ({ PidGains(kP, kI, kD, Gain(topSpeed, maxTurningSpeed)) })
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