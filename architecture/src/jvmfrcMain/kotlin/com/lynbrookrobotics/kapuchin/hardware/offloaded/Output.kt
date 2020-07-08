package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.ControlMode.*
import com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput
import com.ctre.phoenix.motorcontrol.ControlMode.Velocity
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType.*
import info.kunalsheth.units.generated.*

sealed class OffloadedOutput {
    abstract val gains: OffloadedEscGains?
    abstract val safeties: OffloadedEscSafeties
    abstract val config: OffloadedEscConfiguration
    abstract val mode: ControlMode
    abstract val value: Double

    abstract fun with(safeties: OffloadedEscSafeties): OffloadedOutput
    abstract fun with(config: OffloadedEscConfiguration): OffloadedOutput

    fun writeTo(esc: VictorSPX, timeoutMs: Int = config.timeoutMs) {
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonSRX, timeoutMs: Int = config.timeoutMs) {
        safeties.writeTo(esc, timeoutMs)
        gains?.writeTo(esc, timeoutMs)
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonFX, timeoutMs: Int = config.timeoutMs) {
        safeties.writeTo(esc, timeoutMs)
        gains?.writeTo(esc, timeoutMs)
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: CANSparkMax, pidController: CANPIDController, timeoutMs: Int = config.timeoutMs) {
        +esc.setCANTimeout(timeoutMs)

        safeties.writeTo(esc)
        gains?.writeTo(esc, pidController)
        config.writeTo(esc, pidController)

        +pidController.setReference(value, when (mode) {
            Position -> kPosition
            Velocity -> kVelocity
            PercentOutput -> kDutyCycle
            Current -> kCurrent
            else -> TODO("Implement fancy control types for SparkMAX")
        })
    }
}

data class VelocityOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = Velocity
    override fun with(safeties: OffloadedEscSafeties) = copy(safeties = safeties)
    override fun with(config: OffloadedEscConfiguration) = copy(config = config)
}

data class PositionOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = Position
    override fun with(safeties: OffloadedEscSafeties) = copy(safeties = safeties)
    override fun with(config: OffloadedEscConfiguration) = copy(config = config)
}

data class PercentOutput(
        override val config: OffloadedEscConfiguration,
        val dutyCycle: DutyCycle,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = PercentOutput
    override val value = dutyCycle.Each
    override val gains = null

    override fun with(safeties: OffloadedEscSafeties) = copy(safeties = safeties)
    override fun with(config: OffloadedEscConfiguration) = copy(config = config)
}

data class CurrentOutput(
        override val config: OffloadedEscConfiguration,
        val current: ElectricCurrent,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = Current
    override val value = current.Ampere
    override val gains = null

    override fun with(safeties: OffloadedEscSafeties) = copy(safeties = safeties)
    override fun with(config: OffloadedEscConfiguration) = copy(config = config)
}