package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType
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

    fun writeTo(esc: CANSparkMax) {
        config.writeTo(esc)
        safeties.writeTo(esc)
        gains?.writeTo(esc)
        val controlType = when (this) {
            is PositionOutput -> ControlType.kPosition
            is VelocityOutput -> ControlType.kVelocity
            is PercentOutput -> ControlType.kDutyCycle
            is CurrentOutput -> ControlType.kCurrent
        }
        esc.pidController.setReference(value, controlType)
    }
}

data class VelocityOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = ControlMode.Velocity
}

data class PositionOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = ControlMode.Position
}

data class PercentOutput(
        override val config: OffloadedEscConfiguration,
        val dutyCycle: DutyCycle,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = ControlMode.PercentOutput
    override val value = dutyCycle.Each
    override val gains = null
}

data class CurrentOutput(
        override val config: OffloadedEscConfiguration,
        val current: ElectricCurrent,
        override val safeties: OffloadedEscSafeties = OffloadedEscSafeties.NoSafeties
) : OffloadedOutput() {
    override val mode = ControlMode.Current
    override val value = current.Ampere
    override val gains = null
}