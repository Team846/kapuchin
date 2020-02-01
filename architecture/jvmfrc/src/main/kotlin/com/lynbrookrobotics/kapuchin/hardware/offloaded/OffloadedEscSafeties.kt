package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.SoftLimitDirection.kForward
import com.revrobotics.CANSparkMax.SoftLimitDirection.kReverse
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.ConcurrentHashMap

data class OffloadedEscSafeties(
        val syncThreshold: Time,
        val min: Int?, val max: Int?
) {
    companion object {
        val NoSafeties = OffloadedEscSafeties(0.Second, null, null)
        val talonCache = ConcurrentHashMap<TalonSRX, OffloadedEscSafeties>()
        val sparkMaxCache = ConcurrentHashMap<CANSparkMax, OffloadedEscSafeties>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: TalonSRX, timeoutMs: Int = this.timeoutMs) {
        val cached = talonCache[esc]
        if (this != cached) cached.also {
            println("Writing safeties to TalonSRX ${esc.deviceID}")

            if (it == null || it.min != this.min) {
                +esc.configReverseSoftLimitEnable(min != null, timeoutMs)
                if (min != null) +esc.configReverseSoftLimitThreshold(min, timeoutMs)
            }
            if (it == null || it.max != this.max) {
                +esc.configForwardSoftLimitEnable(max != null, timeoutMs)
                if (max != null) +esc.configForwardSoftLimitThreshold(max, timeoutMs)
            }
        }
        talonCache[esc] = this
    }

    fun writeTo(esc: CANSparkMax) {
        val cached = sparkMaxCache[esc]
        if (this != cached) cached.also {
            println("Writing safeties to CANSparkMax ${esc.deviceId}")

            if (it == null || it.min != min) {
                +esc.enableSoftLimit(kReverse, min != null)
                min?.toFloat()?.let { min ->
                    +esc.setSoftLimit(kReverse, min)
                }
            }
            if (it == null || it.max != max) {
                +esc.enableSoftLimit(kForward, max != null)
                max?.toFloat()?.let { max ->
                    +esc.setSoftLimit(kForward, max)
                }
            }
        }
        sparkMaxCache[esc] = this
    }
}