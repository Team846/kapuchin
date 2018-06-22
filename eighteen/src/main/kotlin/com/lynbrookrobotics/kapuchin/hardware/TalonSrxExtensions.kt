package com.lynbrookrobotics.kapuchin.hardware

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.ControlMode.*
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced.*
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod.Period_5Ms
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.offloaded.LazyOffloadedOutputWriter
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import info.kunalsheth.units.generated.*

fun Named.lazyOutput(talonSRX: TalonSRX, timeout: Time, idx: Int = 0): LazyOffloadedOutputWriter {
    fun wrap(f: (Int, Double, Int) -> ErrorCode): (Double) -> Unit = {
        val err = f(idx, it, timeout.milli(T::Second).toInt())
        if (err != OK) log(Error) { "TalonSRX returned error code $err" }
    }

    return LazyOffloadedOutputWriter(
            writeKp = wrap(talonSRX::config_kP),
            writeKi = wrap(talonSRX::config_kI),
            writeKd = wrap(talonSRX::config_kD),
            writeKf = wrap(talonSRX::config_kF),
            writePercent = { talonSRX.set(PercentOutput, it.Tick) },
            writeCurrent = { talonSRX.set(Current, it.Ampere) },
            writePosition = { talonSRX.set(Position, it) },
            writeVelocity = { talonSRX.set(ControlMode.Velocity, it) }
    )
}

fun generalSetup(esc: BaseMotorController, voltageCompensation: Volt, currentLimit: Ampere) {
    val t = 0

    esc.setNeutralMode(NeutralMode.Coast)
    esc.configOpenloopRamp(0.0, t)
    esc.configClosedloopRamp(0.0, t)

    esc.configPeakOutputReverse(-1.0, t)
    esc.configNominalOutputReverse(0.0, t)
    esc.configNominalOutputForward(0.0, t)
    esc.configPeakOutputForward(1.0, t)
    esc.configNeutralDeadband(0.001 /*min*/, t)

    esc.configVoltageCompSaturation(voltageCompensation.Volt, t)
    esc.configVoltageMeasurementFilter(32, t)
    esc.enableVoltageCompensation(true)

    if (esc is TalonSRX) {
        esc.configContinuousCurrentLimit(currentLimit.Ampere.toInt(), t)
        esc.configPeakCurrentLimit(0, t) // simpler, single-threshold limiting
    }
}

fun configMaster(master: TalonSRX, voltageCompensation: Volt, currentLimit: Ampere, multiIdx: Boolean = false) {
    generalSetup(master, voltageCompensation, currentLimit)
    val t = 0
    val slow = 1000

    StatusFrameEnhanced.values().forEach { master.setStatusFramePeriod(it, slow, t) }

    mapOf(
            Status_1_General to 5, // tells slaves what to output
            Status_2_Feedback0 to 10, // tells RoboRIO about selected sensor data
            Status_12_Feedback1 to if (multiIdx) 10 else slow, // tells RoboRIO about selected sensor data
            Status_13_Base_PIDF0 to 15, // current error, integral, and derivative
            Status_13_Base_PIDF0 to if (multiIdx) 15 else slow // current error, integral, and derivative
    ).forEach { frame, period ->
        master.setStatusFramePeriod(frame, period, t)
    }

    master.configVelocityMeasurementPeriod(Period_5Ms, t)
    master.configVelocityMeasurementWindow(4, t)
}

fun configSlave(slave: BaseMotorController, voltageCompensation: Volt, currentLimit: Ampere, multiIdx: Boolean = false) {
    generalSetup(slave, voltageCompensation, currentLimit)
    val t = 0
    val slow = 1000
    StatusFrame.values().forEach { slave.setStatusFramePeriod(it, slow, t) }
}