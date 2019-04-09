package com.lynbrookrobotics.kapuchin.hardware

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlFrame.Control_3_General
import com.ctre.phoenix.motorcontrol.ControlMode
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


sealed class OffloadedOutput {
    fun writeTo(esc: VictorSPX, config: OffloadedEscConfiguration, mode: ControlMode, value: Double, gains: SlotConfiguration?, f: (VictorSPXConfiguration) -> Unit) {
        if (gains != null) config.victor.slot0 = gains
        +esc.configAllSettings(config.victor.also(f))
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonSRX, config: OffloadedEscConfiguration, mode: ControlMode, value: Double, gains: SlotConfiguration?, f: (TalonSRXConfiguration) -> Unit) {
        if (gains != null) config.talon.slot0 = gains
        +esc.configAllSettings(config.talon.also(f))
        esc.set(mode, value)
    }

    abstract fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit = {})
    abstract fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit = {})
}

data class VelocityOutput(
        val config: OffloadedEscConfiguration,
        val gains: SlotConfiguration,
        val value: Double
) : OffloadedOutput() {
    override fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Velocity, value, gains, f)
    override fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Velocity, value, gains, f)
}

data class PositionOutput(
        val config: OffloadedEscConfiguration,
        val gains: SlotConfiguration,
        val value: Double
) : OffloadedOutput() {
    override fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Position, value, gains, f)
    override fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Position, value, gains, f)
}

data class PercentOutput(
        val config: OffloadedEscConfiguration,
        val value: DutyCycle
) : OffloadedOutput() {
    override fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.PercentOutput, value.Each, null, f)
    override fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.PercentOutput, value.Each, null, f)
}

data class CurrentOutput(
        val config: OffloadedEscConfiguration,
        val value: ElectricCurrent
) : OffloadedOutput() {
    override fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Current, value.Ampere, null, f)
    override fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit) = writeTo(esc, config, ControlMode.Current, value.Ampere, null, f)
}

data class OffloadedEscConfiguration(
        val openloopRamp: Time = 0.Second,
        val closedloopRamp: Time = 0.Second,
        val peakOutputForward: V = 12.Volt,
        val nominalOutputForward: V = 0.Volt,
        val nominalOutputReverse: V = -nominalOutputForward,
        val peakOutputReverse: V = -peakOutputForward,
        val voltageCompSaturation: V = 12.Volt,
        val continuousCurrentLimit: I = 25.Ampere,
        val peakCurrentLimit: I = 40.Ampere,
        val peakCurrentDuration: Time = 1.Second
) {

    val talon by lazy {
        TalonSRXConfiguration().also {
            it.openloopRamp = openloopRamp.Second
            it.closedloopRamp = closedloopRamp.Second

            it.peakOutputForward = (peakOutputForward / voltageCompSaturation).Each
            it.nominalOutputForward = (nominalOutputForward / voltageCompSaturation).Each
            it.nominalOutputReverse = (nominalOutputReverse / voltageCompSaturation).Each
            it.peakOutputReverse = (peakOutputReverse / voltageCompSaturation).Each

            it.voltageCompSaturation = voltageCompSaturation.Volt

            it.continuousCurrentLimit = continuousCurrentLimit.Ampere.toInt()
            it.peakCurrentLimit = peakCurrentLimit.Ampere.toInt()
            it.peakCurrentDuration = peakCurrentDuration.milli(Second).toInt()
        }
    }

    val victor by lazy {
        // copy and paste from `talon`
        VictorSPXConfiguration().also {
            it.openloopRamp = openloopRamp.Second
            it.closedloopRamp = closedloopRamp.Second

            it.peakOutputForward = (peakOutputForward / voltageCompSaturation).Each
            it.nominalOutputForward = (nominalOutputForward / voltageCompSaturation).Each
            it.nominalOutputReverse = (nominalOutputReverse / voltageCompSaturation).Each
            it.peakOutputReverse = (peakOutputReverse / voltageCompSaturation).Each

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

fun SubsystemHardware<*, *>.setupMaster(master: TalonSRX, config: OffloadedEscConfiguration, vararg feedback: FeedbackDevice) {
    generalSetup(master, config)

    feedback.forEachIndexed { i, sensor -> +master.configSelectedFeedbackSensor(sensor, i, configTimeout) }

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}