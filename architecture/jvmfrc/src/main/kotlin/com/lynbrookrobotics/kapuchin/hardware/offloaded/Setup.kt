package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlFrame.Control_3_General
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced.Status_2_Feedback0
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod.Period_5Ms
import com.ctre.phoenix.motorcontrol.can.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.revrobotics.CANError
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.PeriodicFrame
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.io.IOException

val configTimeout = if (HardwareInit.crashOnFailure) 2500 else 0

operator fun ErrorCode.unaryPlus() = checkOk
val ErrorCode.checkOk: Unit
    get() {
        if (this != OK && HardwareInit.crashOnFailure)
            throw IOException("Phoenix call returned $this")
    }

operator fun CANError.unaryPlus() = checkOk
val CANError.checkOk: Unit
    get() {
        if (this != CANError.kOk && HardwareInit.crashOnFailure)
            throw IOException("REV Spark Max call returned $this")
    }

fun RobotHardware<*>.generalSetup(esc: BaseMotorController, config: OffloadedEscConfiguration) {
    +esc.configFactoryDefault(configTimeout)

    esc.setNeutralMode(NeutralMode.Brake)

    +esc.configNeutralDeadband(0.001, configTimeout)
    esc.enableVoltageCompensation(true)

    if (esc is TalonSRX) esc.enableCurrentLimit(true)
    // TalonFX current limiting is already enabled in OffloadedEscConfiguration

    when (esc) {
        is TalonSRX -> config.writeTo(esc, configTimeout)
        is TalonFX -> config.writeTo(esc, configTimeout)
        is VictorSPX -> config.writeTo(esc, configTimeout)
    }
}

fun RobotHardware<*>.generalSetup(esc: CANSparkMax, config: OffloadedEscConfiguration) {
    +esc.setCANTimeout(configTimeout)
    +esc.restoreFactoryDefaults()

    +esc.setIdleMode(IdleMode.kBrake)

    +esc.enableVoltageCompensation(12.0)
    +esc.setSmartCurrentLimit(40)

    config.writeTo(esc, esc.pidController)

    +esc.setCANTimeout(15)
}

fun SubsystemHardware<*, *>.setupMaster(master: BaseTalon, config: OffloadedEscConfiguration, sensor: FeedbackDevice, fastOnboard: Boolean) {
    generalSetup(master, config)

    +master.setControlFramePeriod(Control_3_General, syncThreshold.milli(Second).toInt())

    +master.configSelectedFeedbackSensor(sensor, OffloadedEscGains.idx, configTimeout)
    if (fastOnboard) // https://phoenix-documentation.readthedocs.io/en/latest/ch18_CommonAPI.html#setting-status-frame-periods
        +master.setStatusFramePeriod(Status_2_Feedback0, syncThreshold.milli(Second).toInt())

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}

fun SubsystemHardware<*, *>.setupMaster(master: CANSparkMax, config: OffloadedEscConfiguration, fastOnboard: Boolean) {
    generalSetup(master, config)

    +master.setCANTimeout(configTimeout)

    master.setControlFramePeriodMs(syncThreshold.milli(Second).toInt())
    if (fastOnboard) { // http://www.revrobotics.com/sparkmax-users-manual/#section-3-3-2-1
        +master.setPeriodicFramePeriod(PeriodicFrame.kStatus1, syncThreshold.milli(Second).toInt())
        +master.setPeriodicFramePeriod(PeriodicFrame.kStatus2, syncThreshold.milli(Second).toInt())
    }

    master.encoder.apply {
        +setMeasurementPeriod(5)
        +setAverageDepth(4)
    }
    master.alternateEncoder.apply {
        +setMeasurementPeriod(5)
        +setAverageDepth(4)
    }

    +master.setCANTimeout(15)
}