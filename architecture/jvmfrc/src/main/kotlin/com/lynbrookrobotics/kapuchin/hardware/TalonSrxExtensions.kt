package com.lynbrookrobotics.kapuchin.hardware

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlFrame.Control_3_General
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod.Period_5Ms
import com.ctre.phoenix.motorcontrol.can.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.revrobotics.CANError
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.io.IOException

val configTimeout = if (HardwareInit.crashOnFailure) 1000 else 0
private val slowStatusFrameRate = 1000

operator fun ErrorCode.unaryPlus() = checkOk
val ErrorCode.checkOk: Unit
    get() {
        if (this != OK && HardwareInit.crashOnFailure)
            throw IOException("Phoenix call returned $this")
    }

operator fun CANError.unaryPlus() = checkOk
val CANError.checkOk: Unit
    get() {
        if (this != CANError.kOK && HardwareInit.crashOnFailure)
            throw IOException("REV Spark Max call returned $this")
    }

sealed class OffloadedOutput
data class VelocityOutput(
        val config: OffloadedEscConfiguration,
        val gains: SlotConfiguration,
        val value: Double
) : OffloadedOutput()

data class PositionOutput(
        val config: OffloadedEscConfiguration,
        val gains: SlotConfiguration,
        val value: Double
) : OffloadedOutput()

data class PercentOutput(
        val config: OffloadedEscConfiguration,
        val value: DutyCycle
) : OffloadedOutput()

data class CurrentOutput(
        val config: OffloadedEscConfiguration,
        val value: ElectricCurrent
) : OffloadedOutput()

data class OffloadedEscConfiguration(
        val openloopRamp: Time = 0.Second,
        val closedloopRamp: Time = 0.Second,
        val peakOutputForward: DutyCycle = 100.Percent,
        val nominalOutputForward: DutyCycle = 0.Percent,
        val nominalOutputReverse: DutyCycle = 0.Percent,
        val peakOutputReverse: DutyCycle = -100.Percent,
        val voltageCompSaturation: V = 12.Volt,
        val continuousCurrentLimit: I = 25.Ampere,
        val peakCurrentLimit: I = 40.Ampere,
        val peakCurrentDuration: Time = 1.Second
) {

    val talon by lazy {
        TalonSRXConfiguration().also {
            it.openloopRamp = openloopRamp.Second
            it.closedloopRamp = closedloopRamp.Second

            it.peakOutputForward = peakOutputForward.Each
            it.nominalOutputForward = nominalOutputForward.Each
            it.nominalOutputReverse = nominalOutputReverse.Each
            it.peakOutputReverse = peakOutputReverse.Each

            it.voltageCompSaturation = voltageCompSaturation.Volt

            it.continuousCurrentLimit = continuousCurrentLimit.Ampere.toInt()
            it.peakCurrentLimit = peakCurrentLimit.Ampere.toInt()
            it.peakCurrentDuration = peakCurrentDuration.milli(Second).toInt()
        }
    }

    val victor by lazy {
        VictorSPXConfiguration().also {
            it.openloopRamp = openloopRamp.Second
            it.closedloopRamp = closedloopRamp.Second

            it.peakOutputForward = peakOutputForward.Each
            it.nominalOutputForward = nominalOutputForward.Each
            it.nominalOutputReverse = nominalOutputReverse.Each
            it.peakOutputReverse = peakOutputReverse.Each

            it.voltageCompSaturation = voltageCompSaturation.Volt
        }
    }
}

fun SubsystemHardware<*, *>.generalSetup(esc: BaseMotorController, config: OffloadedEscConfiguration) {

    esc.setNeutralMode(NeutralMode.Brake)

    +esc.configNeutralDeadband(0.001, configTimeout)
    esc.enableVoltageCompensation(true)

    val controlFramePeriod = syncThreshold.milli(Second).toInt()
    +esc.setControlFramePeriod(Control_3_General, controlFramePeriod)

    if (esc is TalonSRX) esc.enableCurrentLimit(true)

    if (esc is TalonSRX) +esc.configAllSettings(config.talon, configTimeout)
    if (esc is VictorSPX) +esc.configAllSettings(config.victor, configTimeout)
}

fun SubsystemHardware<*, *>.configMaster(master: TalonSRX, config: OffloadedEscConfiguration, vararg feedback: FeedbackDevice) {
    generalSetup(master, config)

    feedback.forEachIndexed { i, sensor -> +master.configSelectedFeedbackSensor(sensor, i, configTimeout) }

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}