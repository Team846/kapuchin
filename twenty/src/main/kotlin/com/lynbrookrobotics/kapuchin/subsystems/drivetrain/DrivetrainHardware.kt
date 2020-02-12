package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.SPI
import edu.wpi.first.wpilibj.SerialPort
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>(), GenericDrivetrainHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 3.milli(Second)
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

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    override val conversions = DrivetrainConversions(this)

    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, QuadEncoder, true)
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
        setupMaster(it, escConfig, QuadEncoder, true)
        it.selectedSensorPosition = 0
        it.inverted = rightEscInversion
        it.setSensorPhase(rightSensorInversion)
    }
    val rightSlaveEsc by hardw { VictorSPX(rightSlaveEscId) }.configure {
        generalSetup(it, escConfig)
        it.follow(rightMasterEsc)
        it.inverted = rightEscInversion
    }

    val driftTolerance by pref(0.2, DegreePerSecond)
    private fun waitUntilTrue(
            timeout: Time = 10.Second,
            poll: Time = 0.5.Second,
            f: () -> Boolean
    ): Boolean {
        if (!f()) {
            log(Debug) { "Waiting for predicated to return true..." }
            val startTime = currentTime
            while (!f() && currentTime - startTime < timeout) blockingDelay(poll)
        }
        return f()
    }

    val gyro by hardw { AHRS(SPI.Port.kMXP, 200.toByte()) }.configure {
        it.zeroYaw()
    }.verify("NavX should be connected") {
        waitUntilTrue() { it.isConnected }
    }.verify("NavX should be finished calibrating on startup") {
        waitUntilTrue() { !it.isCalibrating }
    }/*.verify("NavX magnetometer should be calibrated") {
        it.isMagnetometerCalibrated
    }*/.verify("NavX should be configured to update at 200hz") {
        it.actualUpdateRate == 200
    }.verify("RoboRIO should receive NavX updates at 200hz") {
        val desiredUpdates = 10
        val startingIndex = it.updateCount
        blockingDelay(desiredUpdates.Each / 200.Hertz * 1.1)
        it.updateCount > startingIndex + desiredUpdates
    }.verify("NavX yaw should not drift after calibration") {
        it.rate.DegreePerSecond in `±`(driftTolerance)
    }

    private val odometryTicker = ticker(Priority.RealTime, 5.milli(Second), "Odometry")

    private val ticksToSerialPort = "kUSB1"
    private val ticksToSerial by hardw<TicksToSerial?> {
        TicksToSerial(SerialPort.Port.valueOf(ticksToSerialPort))
    }.verify("ticks-to-serial is connected") {
        it!!().forEach {}
        true
    }.otherwise(hardw { null })

    private val t2sNamed = Named("T2S Odometry", this)
    val t2sPosition = ticksToSerial?.let { t2s ->
        sensor {
            t2s().forEach { (l, r) -> conversions.t2sOdometry(l, r) }
            conversions.t2sOdometry.matrixTracking.run { Position(x, y, bearing) } stampWith it
        }
                .with(graph("X Location", Foot, t2sNamed)) { it.x }
                .with(graph("Y Location", Foot, t2sNamed)) { it.y }
                .with(graph("Bearing", Degree, t2sNamed)) { it.bearing }
    }

    private val escNamed = Named("ESC Odometry", this)
    val escPosition = sensor {
        conversions.escOdometry(
                leftMasterEsc.getSelectedSensorPosition(idx),
                rightMasterEsc.getSelectedSensorPosition(idx),
                gyro.yaw.Degree
        )
        conversions.escOdometry.tracking.run { Position(x, y, bearing) } stampWith it
    }
            .with(graph("X Location", Foot, escNamed)) { it.x }
            .with(graph("Y Location", Foot, escNamed)) { it.y }
            .with(graph("Bearing", Degree, escNamed)) { it.bearing }

    override val position = /*t2sPosition ?:*/ escPosition

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

        odometryTicker.runOnTick { time ->
            escPosition.optimizedRead(time, syncThreshold)
        }
    }
}
