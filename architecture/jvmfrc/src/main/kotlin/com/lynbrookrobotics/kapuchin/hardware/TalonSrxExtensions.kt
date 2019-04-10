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
import java.util.concurrent.ConcurrentHashMap

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
    abstract val gains: OffloadedEscGains?
    abstract val config: OffloadedEscConfiguration
    abstract val mode: ControlMode
    abstract val value: Double

    fun writeTo(esc: VictorSPX, f: (VictorSPXConfiguration) -> Unit = {}) {
        config.writeTo(esc)
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonSRX, f: (TalonSRXConfiguration) -> Unit = {}) {
        gains?.writeTo(esc)
        config.writeTo(esc)
        esc.set(mode, value)
    }
}

data class VelocityOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double
) : OffloadedOutput() {
    override val mode = ControlMode.Velocity
}

data class PositionOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double
) : OffloadedOutput() {
    override val mode = ControlMode.Position
}

data class PercentOutput(
        override val config: OffloadedEscConfiguration,
        val dutyCycle: DutyCycle
) : OffloadedOutput() {
    override val mode = ControlMode.PercentOutput
    override val value = dutyCycle.Each
    override val gains = null
}

data class CurrentOutput(
        override val config: OffloadedEscConfiguration,
        val current: ElectricCurrent
) : OffloadedOutput() {
    override val mode = ControlMode.Current
    override val value = current.Ampere
    override val gains = null
}

data class OffloadedEscGains(
        val syncThreshold: Time,
        var kP: Double = 0.0,
        var kI: Double = 0.0,
        var kD: Double = 0.0,
        var kF: Double = 0.0,
        var maxIntegralAccumulator: Double = 0.0
) {
    companion object {
        const val idx = 0
        val talonCache = ConcurrentHashMap<TalonSRX, OffloadedEscGains>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: TalonSRX) {
        talonCache[esc].takeIf { this != it }.also {
            if (it == null || it.kP != this.kP)
                +esc.config_kP(idx, kP, timeoutMs)

            if (it == null || it.kI != this.kI)
                +esc.config_kI(idx, kI, timeoutMs)

            if (it == null || it.kD != this.kD)
                +esc.config_kD(idx, kD, timeoutMs)

            if (it == null || it.kF != this.kF)
                +esc.config_kF(idx, kF, timeoutMs)

            if (it == null || it.maxIntegralAccumulator != this.maxIntegralAccumulator)
                +esc.configMaxIntegralAccumulator(idx, maxIntegralAccumulator, timeoutMs)
        }
        talonCache[esc] = this
    }
}

data class OffloadedEscConfiguration(
        val syncThreshold: Time,
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

    companion object {
        val talonCache = ConcurrentHashMap<TalonSRX, OffloadedEscConfiguration>()
        val victorCache = ConcurrentHashMap<VictorSPX, OffloadedEscConfiguration>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: TalonSRX) {
        talonCache[esc].takeIf { this != it }.also {
            if (it == null || it.openloopRamp != this.openloopRamp)
                +esc.configOpenloopRamp(openloopRamp.Second, timeoutMs)

            if (it == null || it.closedloopRamp != this.closedloopRamp)
                +esc.configClosedloopRamp(closedloopRamp.Second, timeoutMs)

            if (it == null || it.peakOutputForward != this.peakOutputForward)
                +esc.configPeakOutputForward((peakOutputForward / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.nominalOutputForward != this.nominalOutputForward)
                +esc.configNominalOutputForward((nominalOutputForward / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.nominalOutputReverse != this.nominalOutputReverse)
                +esc.configNominalOutputReverse((nominalOutputReverse / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.peakOutputReverse != this.peakOutputReverse)
                +esc.configPeakOutputReverse((peakOutputReverse / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.voltageCompSaturation != this.voltageCompSaturation)
                +esc.configVoltageCompSaturation(voltageCompSaturation.Volt, timeoutMs)

            if (it == null || it.continuousCurrentLimit != this.continuousCurrentLimit)
                +esc.configContinuousCurrentLimit(continuousCurrentLimit.Ampere.toInt(), timeoutMs)

            if (it == null || it.peakCurrentLimit != this.peakCurrentLimit)
                +esc.configPeakCurrentLimit(peakCurrentLimit.Ampere.toInt(), timeoutMs)

            if (it == null || it.peakCurrentDuration != this.peakCurrentDuration)
                +esc.configPeakCurrentDuration(peakCurrentDuration.milli(Second).toInt(), timeoutMs)
        }
        talonCache[esc] = this
    }

    fun writeTo(esc: VictorSPX) {
        // copy and paste from `talon`
        victorCache[esc].takeIf { this != it }.also {
            if (it == null || it.openloopRamp != this.openloopRamp)
                +esc.configOpenloopRamp(openloopRamp.Second, timeoutMs)

            if (it == null || it.closedloopRamp != this.closedloopRamp)
                +esc.configClosedloopRamp(closedloopRamp.Second, timeoutMs)

            if (it == null || it.peakOutputForward != this.peakOutputForward)
                +esc.configPeakOutputForward((peakOutputForward / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.nominalOutputForward != this.nominalOutputForward)
                +esc.configNominalOutputForward((nominalOutputForward / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.nominalOutputReverse != this.nominalOutputReverse)
                +esc.configNominalOutputReverse((nominalOutputReverse / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.peakOutputReverse != this.peakOutputReverse)
                +esc.configPeakOutputReverse((peakOutputReverse / voltageCompSaturation).Each, timeoutMs)

            if (it == null || it.voltageCompSaturation != this.voltageCompSaturation)
                +esc.configVoltageCompSaturation(voltageCompSaturation.Volt, timeoutMs)
        }
        victorCache[esc] = this
    }
}

fun RobotHardware<*>.generalSetup(esc: BaseMotorController, config: OffloadedEscConfiguration) {
    +esc.configFactoryDefault(configTimeout)

    esc.setNeutralMode(NeutralMode.Brake)

    +esc.configNeutralDeadband(0.001, configTimeout)
    esc.enableVoltageCompensation(true)

    if (esc is TalonSRX) esc.enableCurrentLimit(true)

    if (esc is TalonSRX) config.writeTo(esc)
    if (esc is VictorSPX) config.writeTo(esc)
}

fun SubsystemHardware<*, *>.setupMaster(master: TalonSRX, config: OffloadedEscConfiguration, vararg feedback: FeedbackDevice) {
    generalSetup(master, config)

    +master.setControlFramePeriod(Control_3_General, syncThreshold.milli(Second).toInt())
    feedback.forEachIndexed { i, sensor -> +master.configSelectedFeedbackSensor(sensor, i, configTimeout) }

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}