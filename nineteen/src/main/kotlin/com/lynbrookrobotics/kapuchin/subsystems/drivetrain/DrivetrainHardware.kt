package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.MotorSafety
import edu.wpi.first.wpilibj.SerialPort
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 3.milli(Second)
    override val name = "Drivetrain"

    private val idx = 0

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(30, Ampere)
    val startupFrictionCompensation by pref(1.4, Volt)

    val leftSlaveEscId by pref(11)
    val rightSlaveEscId by pref(13)
    val rightMasterEscId by pref(12)
    val leftMasterEscId by pref(10)

    val leftEscInversion by pref(false)
    val rightEscInversion by pref(true)

    val leftMasterEsc by hardw { WPI_TalonSRX(leftMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, QuadEncoder)
        it.selectedSensorPosition = 0
        it.inverted = leftEscInversion
        it.isSafetyEnabled = false
    }
    val leftSlaveEsc by hardw { WPI_VictorSPX(leftSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit, startupFrictionCompensation)
        it.follow(leftMasterEsc)
        it.inverted = leftEscInversion
        it.isSafetyEnabled = false
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc)

    val rightMasterEsc by hardw { WPI_TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, QuadEncoder)
        it.selectedSensorPosition = 0
        it.inverted = rightEscInversion
        it.isSafetyEnabled = false
    }
    val rightSlaveEsc by hardw { WPI_VictorSPX(rightSlaveEscId) }.configure {
        configSlave(it, operatingVoltage, currentLimit, startupFrictionCompensation)
        it.follow(rightMasterEsc)
        it.inverted = rightEscInversion
        it.isSafetyEnabled = false
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc)


    private val leftEncoderA by pref(0)
    private val rightEncoderA by pref(1)
    private val ticksToSerialPort by pref("kUSB1")

    private val leftEncoder by hardw { Counter(leftEncoderA) }.configure {
        it.setMaxPeriod(0.1)
    }
    private val rightEncoder by hardw { Counter(rightEncoderA) }.configure {
        it.setMaxPeriod(0.1)
    }
    private val ticksToSerial by hardw { TicksToSerial(SerialPort.Port.valueOf(ticksToSerialPort)) }

    val conversions = DrivetrainConversions(this)


    val position = sensor {
        ticksToSerial().forEach { (l, r) -> conversions.accumulateOdometry(l, r) }
        conversions.matrixTracking.run { Position(this.x, this.y, this.bearing) } stampWith it
    }
            .with(graph("X Location", Foot)) { it.x }
            .with(graph("Y Location", Foot)) { it.y }
            .with(graph("Encoder Bearing", Degree)) { it.bearing }

    val leftPosition = sensor {
        conversions.toLeftPosition(
                leftMasterEsc.getSelectedSensorPosition(idx) /
                        conversions.nativeEncoderCountMultiplier
        ) stampWith it
    }.with(graph("Left Position", Foot))

    val rightPosition = sensor {
        conversions.toRightPosition(
                rightMasterEsc.getSelectedSensorPosition(idx) /
                        conversions.nativeEncoderCountMultiplier
        ) stampWith it
    }.with(graph("Right Position", Foot))

    val leftSpeed = sensor {
        conversions.toLeftSpeed(leftEncoder.period.Second) stampWith it
    }.with(graph("Left Speed", FootPerSecond))

    val rightSpeed = sensor {
        conversions.toRightSpeed(rightEncoder.period.Second) stampWith it
    }.with(graph("Right Speed", FootPerSecond))


// todo: Causes major lag on the v13 RoboRIO image. DO NOT USE.
//    private val driftTolerance by pref(1, DegreePerSecond)
//    private var startingAngle = 0.Degree
//    val imu by hardw { ADIS16448_IMU() }
//            .configure { startingAngle = it.angle.Degree }
//            .verify("Gyro should not drift after calibration") {
//                it.rate.DegreePerSecond in 0.DegreePerSecond `±` driftTolerance
//            }
//    val gyroInput = sensor(imu) {
//        GyroInput(angleZ.Degree - startingAngle, rate.DegreePerSecond, accelZ.DegreePerSecondSquared) stampWith it // lastSampleTime returns 0 ?
//    }
//            .with(graph("Bearing", Degree)) { it.angle }
//            .with(graph("Angular Velocity", DegreePerSecond)) { it.velocity }

    init {
        EventLoop.runOnTick { time ->
            setOf(position, leftSpeed, rightSpeed, leftPosition, rightPosition).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}
