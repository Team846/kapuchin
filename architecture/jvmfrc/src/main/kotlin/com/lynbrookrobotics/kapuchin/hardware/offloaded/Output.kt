package com.lynbrookrobotics.kapuchin.hardware.offloaded

import info.kunalsheth.units.generated.*

sealed class OffloadedOutput {
    abstract val gains: OffloadedEscGains?
    abstract val config: OffloadedEscConfiguration
    abstract val mode: com.ctre.phoenix.motorcontrol.ControlMode
    abstract val value: Double

    fun writeTo(esc: com.ctre.phoenix.motorcontrol.can.VictorSPX, f: (com.ctre.phoenix.motorcontrol.can.VictorSPXConfiguration) -> Unit = {}) {
        config.writeTo(esc)
        esc.set(mode, value)
    }

    fun writeTo(esc: com.ctre.phoenix.motorcontrol.can.TalonSRX, f: (com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration) -> Unit = {}) {
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
    override val mode = com.ctre.phoenix.motorcontrol.ControlMode.Velocity
}

data class PositionOutput(
        override val config: OffloadedEscConfiguration,
        override val gains: OffloadedEscGains,
        override val value: Double
) : OffloadedOutput() {
    override val mode = com.ctre.phoenix.motorcontrol.ControlMode.Position
}

data class PercentOutput(
        override val config: OffloadedEscConfiguration,
        val dutyCycle: info.kunalsheth.units.generated.DutyCycle
) : OffloadedOutput() {
    override val mode = com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput
    override val value = dutyCycle.Each
    override val gains = null
}

data class CurrentOutput(
        override val config: OffloadedEscConfiguration,
        val current: info.kunalsheth.units.generated.ElectricCurrent
) : OffloadedOutput() {
    override val mode = com.ctre.phoenix.motorcontrol.ControlMode.Current
    override val value = current.Ampere
    override val gains = null
}