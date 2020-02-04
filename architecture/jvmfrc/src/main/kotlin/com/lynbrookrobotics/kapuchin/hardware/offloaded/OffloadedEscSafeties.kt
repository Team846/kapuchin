package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.SoftLimitDirection
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.ConcurrentHashMap

data class OffloadedEscSafeties(
        val syncThreshold: Time,
        val min: Double?, val max: Double?
) {
    companion object {
        val NoSafeties = OffloadedEscSafeties(0.Second, null, null)
        val talonCache = ConcurrentHashMap<TalonSRX, OffloadedEscSafeties>()
        val sparkCache = ConcurrentHashMap<CANSparkMax, OffloadedEscSafeties>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: TalonSRX, timeoutMs: Int = this.timeoutMs) {
        val cached = talonCache[esc]
        if (this != cached) cached.also {
            println("Writing safeties to TalonSRX ${esc.deviceID}")

            if (it == null || it.min != this.min) {
                +esc.configReverseSoftLimitEnable(min != null, timeoutMs)
                if (min != null) +esc.configReverseSoftLimitThreshold(min.toInt(), timeoutMs)
            }
            if (it == null || it.max != this.max) {
                +esc.configForwardSoftLimitEnable(max != null, timeoutMs)
                if (max != null) +esc.configForwardSoftLimitThreshold(max.toInt(), timeoutMs)
            }
        }
        talonCache[esc] = this
    }

    fun writeTo(esc: CANSparkMax) {
        val cached = sparkCache[esc]
        if (this != cached) cached.also {
            println("Writing safeties to SparkMAX ${esc.deviceId}")

            if (it == null || it.min != this.min) {
                +esc.enableSoftLimit(SoftLimitDirection.kReverse, min != null)
                if (min != null) +esc.setSoftLimit(SoftLimitDirection.kReverse, min.toFloat())
            }
            if (it == null || it.max != this.max) {
                +esc.enableSoftLimit(SoftLimitDirection.kForward, max != null)
                if (max != null) +esc.setSoftLimit(SoftLimitDirection.kForward, max.toFloat())
            }
        }
        sparkCache[esc] = this
    }
}