package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainConversions
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.*
import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*

class SwerveHardware(
) : SubsystemHardware<SwerveHardware, SwerveComponent>(), GenericDriveHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "Drivetrain"

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val TREscInversion by pref(false)
    private val TLEscInversion by pref(true)
    private val BREscInversion by pref(true)
    private val BLEscInversion by pref(true)

    private val TRSensorInversion by pref(true)
    private val TLSensorInversion by pref(false)
    private val BRSensorInversion by pref(false)
    private val BLSensorInversion by pref(false)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,
        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    private val idx = 0
    private val TRId = 30
    private val TLId = 32
    private val BRId = 33
    private val BLId = 31

    override val conversions = SwerveConversions(this)

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val topRight by hardw {TalonFX(TRId)}.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = TREscInversion
        it.setSensorPhase(TRSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val topLeft by hardw {TalonFX(TLId)}.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = TLEscInversion
        it.setSensorPhase(TLSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val bottomRight by hardw {TalonFX(BRId)}.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = BREscInversion
        it.setSensorPhase(BRSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val bottomLeft by hardw {TalonFX(BLId)}.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = BLEscInversion
        it.setSensorPhase(BLSensorInversion)
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

    override val position = sensor {
        conversions.tracking.run { Position(x, y, bearing) } stampWith it
    }
        .with(graph("X Location", Foot, escNamed)) { it.x }
        .with(graph("Y Location", Foot, escNamed)) { it.y }
        .with(graph("Bearing", Degree, escNamed)) { it.bearing }

}