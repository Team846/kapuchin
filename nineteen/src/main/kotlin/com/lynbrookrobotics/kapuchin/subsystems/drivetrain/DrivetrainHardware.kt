package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.SerialPort
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 15.milli(Second)
    override val name = "Drivetrain"

    private val idx = 0

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val leftMasterEscId = 10
    val leftSlaveEscId = 11
    val rightMasterEscId = 12
    val rightSlaveEscId = 13

    val leftEscInversion by pref(false)
    val rightEscInversion by pref(true)
    val leftSensorInversion by pref(true)
    val rightSensorInversion by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 15.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val conversions = DrivetrainConversions(this)

    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, QuadEncoder)
        it.selectedSensorPosition = 0
        it.inverted = leftEscInversion
        it.setSensorPhase(leftSensorInversion)
    }
    val leftSlaveEsc by hardw { VictorSPX(leftSlaveEscId) }.configure {
        generalSetup(it, escConfig)
        it.follow(leftMasterEsc)
        it.inverted = leftEscInversion
    }

    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        setupMaster(it, escConfig, QuadEncoder)
        it.selectedSensorPosition = 0
        it.inverted = rightEscInversion
        it.setSensorPhase(rightSensorInversion)
    }
    val rightSlaveEsc by hardw { VictorSPX(rightSlaveEscId) }.configure {
        generalSetup(it, escConfig)
        it.follow(rightMasterEsc)
        it.inverted = rightEscInversion
    }

    private val ticksToSerialPort = "kUSB1"
    private val ticksToSerial by hardw { TicksToSerial(SerialPort.Port.valueOf(ticksToSerialPort)) }

    val position = sensor {
        ticksToSerial().forEach { (l, r) -> conversions.accumulateOdometry(l, r) }
        conversions.matrixTracking.run { Position(x, y, bearing) } stampWith it
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
        conversions.nativeConversion.realVelocity(
                leftMasterEsc.getSelectedSensorVelocity(idx)
        ) stampWith it
    }.with(graph("Left Speed", FootPerSecond))

    val rightSpeed = sensor {
        conversions.nativeConversion.realVelocity(
                rightMasterEsc.getSelectedSensorVelocity(idx)
        ) stampWith it
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
        uiBaselineTicker.runOnTick { time ->
            setOf(leftSpeed, rightSpeed, leftPosition, rightPosition).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }

        EventLoop.runOnTick { time ->
            position.optimizedRead(time, period)
        }
    }
}
