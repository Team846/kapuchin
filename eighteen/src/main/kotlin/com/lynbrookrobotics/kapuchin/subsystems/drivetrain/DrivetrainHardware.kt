package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(Second)
    override val syncThreshold = 3.milli(Second)
    override val subsystemName = "Drivetrain"

    val leftSlaveEscId by pref(14)
    val rightSlaveEscId by pref(13)
    val rightMasterEscId by pref(11)
    val leftMasterEscId by pref(12)


    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(10, Ampere)


    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, QuadEncoder)
    }
    val leftSlaveEsc by hardw { VictorSPX(leftSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(leftMasterEsc)
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc)


    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, QuadEncoder)
        it.inverted = true
    }
    val rightSlaveEsc by hardw { VictorSPX(rightSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit)
        it.follow(rightMasterEsc)
        it.inverted = true
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc)


    val wheelDiameter by pref(6, Inch)

    val encoderToWheelGears by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        ({ GearTrain(encoderGear, wheelGear) })
    }

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(4096)
        val perFeedbackQuantity by pref(1, Turn)
        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage, nativeFeedbackUnits = nativeFeedbackUnits,
                    perFeedbackQuantity = wheelDiameter * PI * encoderToWheelGears.inputToOutput(perFeedbackQuantity).Turn
            )
        })
    }

    val idx = 0
    val position = sensor {
        TwoSided(
                offloadedSettings.realPosition(leftMasterEsc.getSelectedSensorPosition(idx)),
                offloadedSettings.realPosition(rightMasterEsc.getSelectedSensorPosition(idx))
        ) stampWith it
    }
//            .with(graph("Left Position", Foot)) { it.left }
//            .with(graph("Right Position", Foot)) { it.right }

    val velocity = sensor {
        TwoSided(
                offloadedSettings.realVelocity(leftMasterEsc.getSelectedSensorVelocity(idx)),
                offloadedSettings.realVelocity(rightMasterEsc.getSelectedSensorVelocity(idx))
        ) stampWith it
    }//.with(graph("Forward Velocity", FootPerSecond)) { it.avg }

    val driftTolerance by pref(1, DegreePerSecond)
    private lateinit var startingAngle: Angle
    val gyro by hardw { ADIS16448_IMU() }
            .configure { startingAngle = it.angle.Degree }
            .verify("Gyro should not drift after calibration") {
                it.rate.DegreePerSecond in 0.DegreePerSecond `±` driftTolerance
            }
    val gyroInput = sensor(gyro) {
        GyroInput(angleZ.Degree - startingAngle, rate.DegreePerSecond, accelZ.DegreePerSecondSquared) stampWith it // lastSampleTime returns 0 ?
    }
//            .with(graph("Bearing", Degree)) { it.angle }
//            .with(graph("Angular Velocity", DegreePerSecond)) { it.velocity }
}