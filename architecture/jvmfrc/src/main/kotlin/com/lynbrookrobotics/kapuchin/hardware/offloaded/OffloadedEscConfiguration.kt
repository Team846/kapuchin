package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.revrobotics.CANSparkMax
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.ConcurrentHashMap

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
        val sparkCache = ConcurrentHashMap<CANSparkMax, OffloadedEscConfiguration>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    private fun configureEsc(it: OffloadedEscConfiguration?, esc: BaseMotorController, timeoutMs: Int) {
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

    fun writeTo(esc: TalonSRX, timeoutMs: Int = this.timeoutMs) {
        val cached = talonCache[esc]
        if (this != cached) talonCache[esc].also {
            println("Writing configurations to TalonSRX ${esc.deviceID}")

            configureEsc(it, esc, timeoutMs)

            if (it == null || it.continuousCurrentLimit != this.continuousCurrentLimit)
                +esc.configContinuousCurrentLimit(continuousCurrentLimit.Ampere.toInt(), timeoutMs)

            if (it == null || it.peakCurrentLimit != this.peakCurrentLimit)
                +esc.configPeakCurrentLimit(peakCurrentLimit.Ampere.toInt(), timeoutMs)

            if (it == null || it.peakCurrentDuration != this.peakCurrentDuration)
                +esc.configPeakCurrentDuration(peakCurrentDuration.milli(Second).toInt(), timeoutMs)
        }
        talonCache[esc] = this
    }

    fun writeTo(esc: VictorSPX, timeoutMs: Int = this.timeoutMs) {
        // copy and paste from `talon`
        val cached = victorCache[esc]
        if (this != cached) victorCache[esc].also {
            println("Writing configurations to VictorSPX ${esc.deviceID}")

            configureEsc(it, esc, timeoutMs)
        }
        victorCache[esc] = this
    }

    fun writeTo(esc: CANSparkMax, timeoutMs: Int = this.timeoutMs) {
        val cached = sparkCache[esc]
        if (this != cached) sparkCache[esc].also {
            println("Writing configurations to CANSparkMAX ${esc.deviceId}")

            if (it == null || it.openloopRamp != this.openloopRamp)
                esc.rampRate = openloopRamp.Second

            if (it == null || it.closedloopRamp != this.closedloopRamp)
                +esc.setRampRate(openloopRamp.Second)

            sparkCache[esc] = this
        }
    }
}