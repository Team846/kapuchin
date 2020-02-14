package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.ControlMode.*
import com.ctre.phoenix.motorcontrol.ControlMode.Velocity
import com.ctre.phoenix.motorcontrol.can.BaseTalon
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

    fun with(safeties: OffloadedEscSafeties) = when (this) {
        is PositionOutput -> copy(safeties = safeties)
        is VelocityOutput -> copy(safeties = safeties)
        is PercentOutput -> copy(safeties = safeties)
        is CurrentOutput -> copy(safeties = safeties)
    }

    fun writeTo(esc: VictorSPX, timeoutMs: Int = 15) {
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonSRX, timeoutMs: Int = 15) {
        safeties.writeTo(esc, timeoutMs)
        gains?.writeTo(esc, timeoutMs)
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: TalonFX, timeoutMs: Int = 15) {
        safeties.writeTo(esc, timeoutMs)
        gains?.writeTo(esc, timeoutMs)
        config.writeTo(esc, timeoutMs)
        esc.set(mode, value)
    }

    fun writeTo(esc: CANSparkMax, pidController: CANPIDController, timeoutMs: Int = 15) {
        +esc.setCANTimeout(timeoutMs)

        safeties.writeTo(esc)
        gains?.writeTo(esc, pidController)
        config.writeTo(esc, pidController)

        +pidController.setReference(value, when(mode) {
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
}

data class PositionOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = Position
}

data class PercentOutput(
        override val config: OffloadedEscConfiguration,
        val dutyCycle: DutyCycle,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = PercentOutput
    override val value = dutyCycle.Each
    override val gains = null
}

data class CurrentOutput(
        override val config: OffloadedEscConfiguration,
        val current: ElectricCurrent,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = Current
    override val value = current.Ampere
    override val gains = null
}