package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.SPI
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>(), GenericDrivetrainHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "Drivetrain"

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val leftEscInversion by pref(false)
    private val rightEscInversion by pref(true)
    private val leftSensorInversion by pref(true)
    private val rightSensorInversion by pref(false)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val escConfig by escConfigPref(
            defaultNominalOutput = 1.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val idx = 0
    private val leftMasterEscId = 30
    private val leftSlaveEscId = 31
    private val rightMasterEscId = 32
    private val rightSlaveEscId = 33

    override val conversions = DrivetrainConversions(this)

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val leftMasterEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0)
        it.inverted = leftEscInversion
        it.setSensorPhase(leftSensorInversion)
        it.setNeutralMode(NeutralMode.Coast)
    }
    val leftSlaveEsc by hardw { TalonFX(leftSlaveEscId) }.configure {
        generalSetup(it, escConfig)
        it.follow(leftMasterEsc)
        it.inverted = leftEscInversion
        it.setNeutralMode(NeutralMode.Coast)
    }

    val rightMasterEsc by hardw { TalonFX(rightMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0)
        it.inverted = rightEscInversion
        it.setSensorPhase(rightSensorInversion)
        it.setNeutralMode(NeutralMode.Coast)
    }
    val rightSlaveEsc by hardw { TalonFX(rightSlaveEscId) }.configure {
        generalSetup(it, escConfig)
        it.follow(rightMasterEsc)
        it.inverted = rightEscInversion
        it.setNeutralMode(NeutralMode.Coast)
    }

    private val gyro by hardw { AHRS(SPI.Port.kMXP, 200.toByte()) }.configure {
        blockUntil() { it.isConnected }
        blockUntil() { !it.isCalibrating }
        it.zeroYaw()
    }.verify("NavX should be connected") {
        it.isConnected
    }.verify("NavX should be finished calibrating on startup") {
        !it.isCalibrating
    }/*.verify("NavX magnetometer should be calibrated") {
        it.isMagnetometerCalibrated
    }*/.verify("NavX should be configured to update at 200hz") {
        it.actualUpdateRate == 200
    }/*.verify("RoboRIO should receive NavX updates at 200hz") {
        val desiredUpdates = 10
        val startingIndex = it.updateCount
        blockingDelay(desiredUpdates.Each / 200.Hertz * 1.1)
        it.updateCount > startingIndex + desiredUpdates
    }*/.verify("NavX yaw should not drift after calibration") {
        it.rate.DegreePerSecond in `±`(driftTolerance)
    }

    private val odometryTicker = ticker(Priority.RealTime, 5.milli(Second), "Odometry")

    private val escNamed = Named("ESC Odometry", this)
    override val position = sensor {
        conversions.odometry(
                leftPosition.optimizedRead(it, syncThreshold).y,
                rightPosition.optimizedRead(it, syncThreshold).y,
                gyro.yaw.Degree
        )
        conversions.tracking.run { Position(x, y, bearing) } stampWith it
    }
            .with(graph("X Location", Foot, escNamed)) { it.x }
            .with(graph("Y Location", Foot, escNamed)) { it.y }
            .with(graph("Bearing", Degree, escNamed)) { it.bearing }


    val leftPosition = sensor {
        conversions.encoder.right.realPosition(
                leftMasterEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Left Position", Foot))

    val rightPosition = sensor {
        conversions.encoder.left.realPosition(
                rightMasterEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Right Position", Foot))

    val leftSpeed = sensor {
        conversions.encoder.left.realVelocity(
                leftMasterEsc.getSelectedSensorVelocity(idx)
        ) stampWith it
    }.with(graph("Left Speed", FootPerSecond))

    val rightSpeed = sensor {
        conversions.encoder.right.realVelocity(
                rightMasterEsc.getSelectedSensorVelocity(idx)
        ) stampWith it
    }.with(graph("Right Speed", FootPerSecond))

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(leftSpeed, rightSpeed, leftPosition, rightPosition).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }

        odometryTicker.runOnTick { time ->
            conversions.odometry(
                    leftPosition.optimizedRead(time, syncThreshold).y,
                    rightPosition.optimizedRead(time, syncThreshold).y,
                    gyro.yaw.Degree
            )
        }
    }
}
