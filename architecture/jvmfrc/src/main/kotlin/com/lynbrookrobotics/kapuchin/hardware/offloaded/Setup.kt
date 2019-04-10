package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ErrorCode.OK
import com.ctre.phoenix.motorcontrol.ControlFrame.Control_3_General
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod.Period_5Ms
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.hardware.*
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

fun RobotHardware<*>.generalSetup(esc: BaseMotorController, config: OffloadedEscConfiguration) {
    +esc.configFactoryDefault(configTimeout)

    esc.setNeutralMode(NeutralMode.Brake)

    +esc.configNeutralDeadband(0.001, configTimeout)
    esc.enableVoltageCompensation(true)

    if (esc is TalonSRX) esc.enableCurrentLimit(true)

    if (esc is TalonSRX) config.writeTo(esc, configTimeout)
    if (esc is VictorSPX) config.writeTo(esc, configTimeout)
}

fun SubsystemHardware<*, *>.setupMaster(master: TalonSRX, config: OffloadedEscConfiguration, vararg feedback: FeedbackDevice) {
    generalSetup(master, config)

    +master.setControlFramePeriod(Control_3_General, syncThreshold.milli(Second).toInt())
    feedback.forEachIndexed { i, sensor -> +master.configSelectedFeedbackSensor(sensor, i, configTimeout) }

    +master.configVelocityMeasurementPeriod(Period_5Ms, configTimeout)
    +master.configVelocityMeasurementWindow(4, configTimeout)
}