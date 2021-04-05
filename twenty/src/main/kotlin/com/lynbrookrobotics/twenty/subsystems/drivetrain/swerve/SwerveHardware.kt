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
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainConversions
import edu.wpi.first.wpilibj.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveHardware(
) : SubsystemHardware<SwerveHardware, SwerveComponent>(), GenericDriveHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "SwerveDrive"

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val frontLeftEscInversion by pref(false)
    private val frontRightEscInversion by pref(true)
    private val backLeftEscInversion by pref(false)
    private val backRightEscInversion by pref(true)

    private val frontLeftSensorInversion by pref(true)
    private val frontRightSensorInversion by pref(false)
    private val backLeftSensorInversion by pref(true)
    private val backRightSensorInversion by pref(false)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,

        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    private val idx = 0 //need to check this later
    private val frontLeftMasterEscId = 30
    private val frontRightSlaveEscId = 32
    private val backLeftMasterEscId = 33
    private val backRightSlaveEscId = 31

    override val conversions = SwerveConversions(this)

    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val frontRightMasterEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = frontLeftEscInversion
        it.setSensorPhase(frontLeftSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val frontLeftMasterEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = frontLeftEscInversion
        it.setSensorPhase(frontLeftSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backRightMasterEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = backRightEscInversion
        it.setSensorPhase(backRightSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
    val backLeftMasterEsc by hardw { TalonFX(leftMasterEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = backLeftEscInversion
        it.setSensorPhase(backLeftSensorInversion)
        it.setNeutralMode(NeutralMode.Brake)
    }
}