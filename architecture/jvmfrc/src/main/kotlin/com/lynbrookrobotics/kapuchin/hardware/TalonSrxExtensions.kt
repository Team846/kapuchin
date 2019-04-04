package com.lynbrookrobotics.kapuchin.hardware

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlFrame.Control_3_General
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.ControlMode.*
import com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod.Period_5Ms
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
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

fun SubsystemHardware<*, *>.lazyOutput(talonSRX: TalonSRX, idx: Int = 0): (OffloadedOutput) -> Unit {
    val gainConfigTimeout = (period / 2).milli(Second).toInt()
    fun wrap(f: (Int, Double, Int) -> ErrorCode): (Double) -> Unit = { f(idx, it, gainConfigTimeout) }

    return lazyOffloadedGainWriter(
            writeKp = wrap(talonSRX::config_kP),
            writeKi = wrap(talonSRX::config_kI),
            writeKd = wrap(talonSRX::config_kD),
            writeKf = wrap(talonSRX::config_kF),
            writePercent = { talonSRX.set(PercentOutput, it.Each) },
            writeCurrent = { talonSRX.set(Current, it.Ampere) },
            writePosition = { talonSRX.set(Position, it) },
            writeVelocity = { talonSRX.set(ControlMode.Velocity, it) }
    )
}

fun SubsystemHardware<*, *>.generalSetup(esc: BaseMotorController, voltageCompensation: V, currentLimit: I, startupFrictionCompensation: V) {
    +esc.configFactoryDefault(configTimeout)

    esc.setNeutralMode(NeutralMode.Brake)
    +esc.configOpenloopRamp(0.0, configTimeout)
    +esc.configClosedloopRamp(0.0, configTimeout)

    val minOutput = (startupFrictionCompensation / voltageCompensation).Each

    +esc.configPeakOutputReverse(-1.0, configTimeout)
    +esc.configNominalOutputReverse(-minOutput, configTimeout)
    +esc.configNominalOutputForward(minOutput, configTimeout)
    +esc.configPeakOutputForward(1.0, configTimeout)
    +esc.configNeutralDeadband(0.001, configTimeout)

    +esc.configVoltageCompSaturation(voltageCompensation.Volt, configTimeout)
    +esc.configVoltageMeasurementFilter(32, configTimeout)
    esc.enableVoltageCompensation(true)

    val controlFramePeriod = syncThreshold.milli(Second).toInt()
    +esc.setControlFramePeriod(Control_3_General, controlFramePeriod)

    if (esc is TalonSRX) {
        +esc.configContinuousCurrentLimit(currentLimit.Ampere.toInt(), configTimeout)
        +esc.configPeakCurrentLimit(0, configTimeout) // simpler, single-threshold limiting
        esc.enableCurrentLimit(true)
    }
}

fun SubsystemHardware<*, *>.configMaster(master: TalonSRX, voltageCompensation: V, currentLimit: I, startupFrictionCompensation: V, vararg feedback: FeedbackDevice) {
    generalSetup(master, voltageCompensation, currentLimit, startupFrictionCompensation)

    feedback.forEachIndexed { i, sensor -> +master.configSelectedFeedbackSensor(sensor, i, configTimeout) }

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}

fun SubsystemHardware<*, *>.configSlave(slave: BaseMotorController, voltageCompensation: V, currentLimit: I, startupFrictionCompensation: V) {
    generalSetup(slave, voltageCompensation, currentLimit, startupFrictionCompensation)
}