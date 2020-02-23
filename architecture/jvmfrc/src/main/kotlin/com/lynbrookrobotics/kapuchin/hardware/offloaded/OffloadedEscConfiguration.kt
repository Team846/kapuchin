package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.ConcurrentHashMap

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

    companion object {
        val cache = ConcurrentHashMap<Any, OffloadedEscConfiguration>()
    }

    private fun writeTo(esc: BaseMotorController, timeoutMs: Int, cached: OffloadedEscConfiguration? = null) {
        if (cached?.openloopRamp != this.openloopRamp)
            +esc.configOpenloopRamp(openloopRamp.Second, timeoutMs)

        if (cached?.closedloopRamp != this.closedloopRamp)
            +esc.configClosedloopRamp(closedloopRamp.Second, timeoutMs)

        if (cached?.peakOutputForward != this.peakOutputForward)
            +esc.configPeakOutputForward((peakOutputForward / voltageCompSaturation).Each, timeoutMs)

        if (cached?.nominalOutputForward != this.nominalOutputForward)
            +esc.configNominalOutputForward((nominalOutputForward / voltageCompSaturation).Each, timeoutMs)

        if (cached?.nominalOutputReverse != this.nominalOutputReverse)
            +esc.configNominalOutputReverse((nominalOutputReverse / voltageCompSaturation).Each, timeoutMs)

        if (cached?.peakOutputReverse != this.peakOutputReverse)
            +esc.configPeakOutputReverse((peakOutputReverse / voltageCompSaturation).Each, timeoutMs)

        if (cached?.voltageCompSaturation != this.voltageCompSaturation)
            +esc.configVoltageCompSaturation(voltageCompSaturation.Volt, timeoutMs)
    }

    fun writeTo(esc: TalonSRX, timeoutMs: Int = 15) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing configurations to TalonSRX ${esc.deviceID}")

            writeTo(esc, timeoutMs, cached)

            if (cached?.continuousCurrentLimit != this.continuousCurrentLimit)
                +esc.configContinuousCurrentLimit(continuousCurrentLimit.Ampere.toInt(), timeoutMs)

            if (cached?.peakCurrentLimit != this.peakCurrentLimit)
                +esc.configPeakCurrentLimit(peakCurrentLimit.Ampere.toInt(), timeoutMs)

            if (cached?.peakCurrentDuration != this.peakCurrentDuration)
                +esc.configPeakCurrentDuration(peakCurrentDuration.milli(Second).toInt(), timeoutMs)
        }
        cache[esc] = this
    }

    fun writeTo(esc: TalonFX, timeoutMs: Int = 15) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing configurations to TalonFX ${esc.deviceID}")

            writeTo(esc, timeoutMs, cached)

            if (cached == null ||
                    cached.continuousCurrentLimit != this.continuousCurrentLimit ||
                    cached.peakCurrentLimit != this.peakCurrentLimit ||
                    cached.peakCurrentDuration != this.peakCurrentDuration
            ) {
                +esc.configStatorCurrentLimit(
                        StatorCurrentLimitConfiguration(
                                true,
                                continuousCurrentLimit.Ampere,
                                peakCurrentLimit.Ampere,
                                peakCurrentDuration.Second
                        ), timeoutMs
                )
                +esc.configSupplyCurrentLimit(
                        SupplyCurrentLimitConfiguration(
                                true,
                                continuousCurrentLimit.Ampere,
                                peakCurrentLimit.Ampere,
                                peakCurrentDuration.Second
                        ), timeoutMs
                )
            }
        }
        cache[esc] = this
    }

    fun writeTo(esc: VictorSPX, timeoutMs: Int = 15) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing configurations to VictorSPX ${esc.deviceID}")

            writeTo(esc, timeoutMs, cached)

            if (cached?.continuousCurrentLimit != this.continuousCurrentLimit)
                println("Cannot write `continuousCurrentLimit` to VictorSPX ${esc.deviceID}")

            if (cached?.peakCurrentLimit != this.peakCurrentLimit)
                println("Cannot write `peakCurrentLimit` to VictorSPX ${esc.deviceID}")

            if (cached?.peakCurrentDuration != this.peakCurrentDuration)
                println("Cannot write `peakCurrentDuration` to VictorSPX ${esc.deviceID}")
        }
        cache[esc] = this
    }

    fun writeTo(esc: CANSparkMax, pidController: CANPIDController) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing configurations to SparkMAX ${esc.deviceId}")

            if (cached?.openloopRamp != this.openloopRamp)
                +esc.setOpenLoopRampRate(openloopRamp.Second)

            if (cached?.closedloopRamp != this.closedloopRamp)
                +esc.setClosedLoopRampRate(closedloopRamp.Second)

            if (cached?.nominalOutputForward != this.nominalOutputForward)
                println("Cannot write `nominalOutputForward` to SparkMAX ${esc.deviceId}")

            if (cached?.nominalOutputReverse != this.nominalOutputReverse)
                println("Cannot write `nominalOutputReverse` to SparkMAX ${esc.deviceId}")

            if (cached == null ||
                    cached.peakOutputReverse != this.peakOutputReverse ||
                    cached.peakOutputForward != this.peakOutputForward
            ) +pidController.setOutputRange(
                    (peakOutputReverse / voltageCompSaturation).Each,
                    (peakOutputForward / voltageCompSaturation).Each
            )

            if (cached?.voltageCompSaturation != this.voltageCompSaturation)
                +esc.enableVoltageCompensation(voltageCompSaturation.Volt)

            if (cached?.continuousCurrentLimit != this.continuousCurrentLimit)
                +esc.setSmartCurrentLimit(continuousCurrentLimit.Ampere.toInt())

            if (cached?.peakCurrentLimit != this.peakCurrentLimit)
                +esc.setSecondaryCurrentLimit(peakCurrentLimit.Ampere)

            if (cached?.peakCurrentDuration != this.peakCurrentDuration)
                println("Cannot write `peakCurrentDuration` to SparkMAX ${esc.deviceId}")
        }
        cache[esc] = this
    }
}