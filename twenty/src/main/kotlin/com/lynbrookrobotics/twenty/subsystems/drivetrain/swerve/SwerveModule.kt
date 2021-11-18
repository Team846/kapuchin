package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.ctre.phoenix.motorcontrol.FeedbackDevice.IntegratedSensor
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.kauailabs.navx.frc.AHRS
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainConversions
import edu.wpi.first.wpilibj.SerialPort.Port.kUSB
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ModuleComponent(hardware: ModuleHardware) : Component<ModuleComponent, ModuleHardware, TwoSided<OffloadedOutput>>(hardware){
    override val fallbackController: ModuleComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(PercentOutput(hardware.escConfig, 0.Percent))
    }

    override fun ModuleHardware.output(value: TwoSided<OffloadedOutput>) {
        value.left.writeTo(hardware.driveEsc)
        value.right.writeTo(hardware.steerEsc)
    }
}

class ModuleHardware(val idx: Int, val driveEscId: Int, val steerEscId: Int) : SubsystemHardware<ModuleHardware, ModuleComponent>() {
    override val name = "Module Hardware"
    override val period =  30.Millisecond
    override val priority = RealTime
    override val syncThreshold = 5.Millisecond

    val driveEscInversion by pref(false)
    val steerEscInversion by pref(false)

    val driveSensorInversion by pref(false)
    val steerSensorInversion by pref(false)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,

        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val conversions = ModuleConversions(this)

    val driveEsc by hardw{ TalonFX(driveEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = driveEscInversion
        it.setSensorPhase(driveSensorInversion)
        it.setNeutralMode(NeutralMode.Coast)
    }

    val steerEsc by hardw{ TalonFX(steerEscId) }.configure {
        setupMaster(it, escConfig, IntegratedSensor, true)
        +it.setSelectedSensorPosition(0.0)
        it.inverted = steerEscInversion
        it.setSensorPhase(steerSensorInversion)
        it.setNeutralMode(NeutralMode.Coast)
    }

    private val gyro by hardw { AHRS(kUSB) }.configure {
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

    val pitch = sensor {
        gyro.pitch.Degree stampWith it
    }.with(graph("Pitch", Degree))

    val drivePosition = sensor {
        conversions.driveEncoder.realPosition(
            driveEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Left Position", Foot))

    val steerPosition = sensor {
        conversions.steerEncoder.realPosition(
            steerEsc.getSelectedSensorPosition(idx)
        ) stampWith it
    }.with(graph("Right Position", Foot))
}

class ModuleConversions(val hardware: ModuleHardware) : Named by Named("Conversions", hardware) {
    val trim by pref(1.0)
    val wheelRadius by pref(3, Inch)

    val driveEncoder by pref {
        val motorGear by pref(18)
        val stage1Gear by pref(50)
        val stage2Gear by pref(16)
        val wheelGear by pref(60)
        val resolution by pref(2048)
        val nativeEncoderCountMultiplier by pref(1)
        ({
            val stage1 = GearTrain(motorGear, stage1Gear)
            val stage2 = GearTrain(stage2Gear, wheelGear)

            val nativeResolution = resolution * nativeEncoderCountMultiplier
            val enc = EncoderConversion(
                nativeResolution,
                stage1.inputToOutput(1.Turn).let(stage2::inputToOutput)
            )

            val gearing = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius * enc.angle(nativeResolution) * trim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )

            gearing
        })
    }

    val steerEncoder by pref {
        val motorGear by pref(18)
        val stage1Gear by pref(50)
        val stage2Gear by pref(16)
        val wheelGear by pref(60)
        val resolution by pref(2048)
        val nativeEncoderCountMultiplier by pref(1)
        ({
            val stage1 = GearTrain(motorGear, stage1Gear)
            val stage2 = GearTrain(stage2Gear, wheelGear)

            val nativeResolution = resolution * nativeEncoderCountMultiplier
            val enc = EncoderConversion(
                nativeResolution,
                stage1.inputToOutput(1.Turn).let(stage2::inputToOutput)
            )

            val gearing = LinearOffloadedNativeConversion(
                ::p, ::p, ::p, ::p,
                nativeOutputUnits = 1023, perOutputQuantity = hardware.escConfig.voltageCompSaturation,
                nativeFeedbackUnits = nativeResolution,
                perFeedbackQuantity = wheelRadius * enc.angle(nativeResolution) * trim / Radian,
                nativeTimeUnit = 100.milli(Second), nativeRateUnit = 1.Second
            )

            gearing
        })
    }
}