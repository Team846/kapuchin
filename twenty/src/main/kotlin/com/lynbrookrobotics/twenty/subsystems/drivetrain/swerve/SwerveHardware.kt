package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveHardware(
) : SubsystemHardware<SwerveHardware, SwerveComponent>(), GenericDriveHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "Drivetrain"

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val fREscInversion by pref(false)
    private val fLEscInversion by pref(true)
    private val bREscInversion by pref(true)
    private val bLEscInversion by pref(true)

    private val fRSensorInversion by pref(true)
    private val fLSensorInversion by pref(false)
    private val bRSensorInversion by pref(false)
    private val bLSensorInversion by pref(false)


    private val fRAngleEscInversion by pref(false)
    private val fLAngleEscInversion by pref(true)
    private val bRAngleEscInversion by pref(true)
    private val bLAngleEscInversion by pref(true)

    private val fRAngleSensorInversion by pref(true)
    private val fLAngleSensorInversion by pref(false)
    private val bRAngleSensorInversion by pref(false)
    private val bLAngleSensorInversion by pref(false)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,
        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    private val idx = 0
    private val fRId = 30
    private val fLId = 32
    private val bRId = 33
    private val bLId = 31

    private val fRAngleId = 30
    private val fLAngleId = 32
    private val bRAngleId = 33
    private val bLAngleId = 31

    override val conversions = SwerveConversions(this)

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val frontRight by hardw { TalonFX(fRId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = fREscInversion
        it.setSensorPhase(fRSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val frontLeft by hardw { TalonFX(fLId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = fLEscInversion
        it.setSensorPhase(fLSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backRight by hardw { TalonFX(bRId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = bREscInversion
        it.setSensorPhase(bRSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backLeft by hardw { TalonFX(bLId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = bLEscInversion
        it.setSensorPhase(bLSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }


    val frontRightAngle by hardw { TalonFX(fRId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = fRAngleEscInversion
        it.setSensorPhase(fRAngleSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val frontLeftAngle by hardw { TalonFX(fLId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = fLAngleEscInversion
        it.setSensorPhase(fLAngleSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backRightAngle by hardw { TalonFX(bRId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = bRAngleEscInversion
        it.setSensorPhase(bRAngleSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backLeftAngle by hardw { TalonFX(bLId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = bLAngleEscInversion
        it.setSensorPhase(bLAngleSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }

    private val gyro by hardw { AHRS(SerialPort.Port.kUSB) }.configure {
        blockUntil() { it.isConnected }
        blockUntil() { !it.isCalibrating }
        it.zeroYaw()
    }.verify("NavX should be connected") {
        it.isConnected
    }.verify("NavX should be finished calibrating on startup") {
        !it.isCalibrating
    }.verify("NavX yaw should not drift after calibration") {
        it.rate.DegreePerSecond in `±`(driftTolerance)
    }

    private val odometryTicker = ticker(Priority.RealTime, 10.milli(Second), "Odometry")
    private val escNamed = Named("ESC Odometry", this)

//    override val position = sensor {
//        //conversions.tracking.run { Position(x, y, bearing) } stampWith it
//        //conversions.odometry(arrayOf(Pair<topRight.>))
//    }
        .with(graph("X Location", Foot, escNamed)) { it.x }
        .with(graph("Y Location", Foot, escNamed)) { it.y }
        .with(graph("Bearing", Degree, escNamed)) { it.bearing }

}