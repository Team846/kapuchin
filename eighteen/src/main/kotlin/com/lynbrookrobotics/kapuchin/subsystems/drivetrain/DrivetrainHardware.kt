package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

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
        configMaster(it, operatingVoltage, currentLimit, QuadEncoder)
    }
    val leftSlaveEsc by hardw { TalonSRX(leftSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(leftMasterEsc)
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc, escCanTimeout)


    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, QuadEncoder)
        it.inverted = true
    }
    val rightSlaveEsc by hardw { TalonSRX(rightSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(rightMasterEsc)
        it.inverted = true
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc, escCanTimeout)

    val wheelDiameter by pref(6::Inch)

    val encoderToWheelGears by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        ({ GearTrain(encoderGear, wheelGear) })
    }

    val offloadedSettings by pref {
        val nativeOutputUnits by pref(1023)
        val perOutputQuantity by pref(12::Volt)
        val nativeFeedbackUnits by pref(4096)
        val perFeedbackQuantity by pref(1::Turn)
        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = nativeOutputUnits, perOutputQuantity = perOutputQuantity, nativeFeedbackUnits = nativeFeedbackUnits,
                    perFeedbackQuantity = wheelDiameter * PI * encoderToWheelGears.inputToOutput(perFeedbackQuantity).Turn
            )
        })
    }

    val idx = 0
    val position = readWithComponent {
        TwoSided(
                offloadedSettings.realPosition(leftMasterEsc.getSelectedSensorPosition(idx)),
                offloadedSettings.realPosition(rightMasterEsc.getSelectedSensorPosition(idx))
        ) stampWith it
    }
    val velocity = readWithComponent {
        TwoSided(
                offloadedSettings.realVelocity(leftMasterEsc.getSelectedSensorVelocity(idx)),
                offloadedSettings.realVelocity(rightMasterEsc.getSelectedSensorVelocity(idx))
        ) stampWith it
    }

    val driftTolerance by pref(1::DegreePerSecond)
    val gyro by hardw { ADIS16448_IMU() }.verify("Gyro should not drift after calibration") {
        it.rate.DegreePerSecond in -driftTolerance..+driftTolerance
    }.readEagerly {
        GyroInput(angle.Degree, rate.DegreePerSecond, accelZ.DegreePerSecondSquared) stampWith lastSampleTime.Second
    }
}