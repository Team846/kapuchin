package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.BaseTalon
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.SoftLimitDirection
import java.util.concurrent.ConcurrentHashMap

data class OffloadedEscSafeties(
        val min: Double?, val max: Double?
) {
    companion object {
        val NoSafeties = OffloadedEscSafeties(null, null)
        val cache = ConcurrentHashMap<Any, OffloadedEscSafeties>()
    }

    fun writeTo(esc: BaseTalon, timeoutMs: Int) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing safeties to Talon${if (esc is TalonSRX) "SRX" else "FX"} ${esc.deviceID}")

            if (cached?.min != this.min) {
                +esc.configReverseSoftLimitEnable(min != null, timeoutMs)
                if (min != null) +esc.configReverseSoftLimitThreshold(min.toInt(), timeoutMs)
            }
            if (cached?.max != this.max) {
                +esc.configForwardSoftLimitEnable(max != null, timeoutMs)
                if (max != null) +esc.configForwardSoftLimitThreshold(max.toInt(), timeoutMs)
            }
        }
        cache[esc] = this
    }

    fun writeTo(esc: CANSparkMax) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing safeties to SparkMAX ${esc.deviceId}")

            if (cached?.min != this.min) {
                +esc.enableSoftLimit(SoftLimitDirection.kReverse, min != null)
                if (min != null) +esc.setSoftLimit(SoftLimitDirection.kReverse, min.toFloat())
            }
            if (cached?.max != this.max) {
                +esc.enableSoftLimit(SoftLimitDirection.kForward, max != null)
                if (max != null) +esc.setSoftLimit(SoftLimitDirection.kForward, max.toFloat())
            }
        }
        cache[esc] = this
    }
}