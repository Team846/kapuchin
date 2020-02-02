package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedEscConfiguration.Companion.sparkMaxControllerCache
import com.revrobotics.CANSparkMax
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.ConcurrentHashMap

data class OffloadedEscGains(
        val syncThreshold: Time,
        var kP: Double = 0.0,
        var kI: Double = 0.0,
        var kD: Double = 0.0,
        var kF: Double = 0.0,
        var maxIntegralAccumulator: Double = 0.0
) {
    companion object {
        const val idx = 0
        val talonCache = ConcurrentHashMap<TalonSRX, OffloadedEscGains>()
        val sparkMaxCache = ConcurrentHashMap<CANSparkMax, OffloadedEscGains>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: TalonSRX, timeoutMs: Int = this.timeoutMs) {
        val cached = talonCache[esc]
        if (this != cached) cached.also {
            println("Writing gains to TalonSRX ${esc.deviceID}")

            if (it == null || it.kP != this.kP)
                +esc.config_kP(idx, kP, timeoutMs)

            if (it == null || it.kI != this.kI)
                +esc.config_kI(idx, kI, timeoutMs)

            if (it == null || it.kD != this.kD)
                +esc.config_kD(idx, kD, timeoutMs)

            if (it == null || it.kF != this.kF)
                +esc.config_kF(idx, kF, timeoutMs)

            if (it == null || it.maxIntegralAccumulator != this.maxIntegralAccumulator)
                +esc.configMaxIntegralAccumulator(idx, maxIntegralAccumulator, timeoutMs)
        }
        talonCache[esc] = this
    }

    fun writeTo(esc: CANSparkMax) {
        val gainsFromCache = sparkMaxCache[esc]
        if (this != gainsFromCache) gainsFromCache.also { cache ->
            println("Writing gains to CANSparkMax ${esc.deviceId}")

            if (cache == null || cache.kP != kP) {
                +sparkMaxControllerCache.getOrPut(esc) { esc.pidController }.setP(kP, idx)
            }
            if (cache == null || cache.kI != kI) {
                +esc.pidController.setI(kI, idx)
            }
            if (cache == null || cache.kD != kD) {
                +esc.pidController.setD(kD, idx)
            }
            if (cache == null || cache.kF != kF) {
                +esc.pidController.setFF(kF, idx)
            }
            if (cache == null || cache.maxIntegralAccumulator != maxIntegralAccumulator) {
                +esc.pidController.setIMaxAccum(maxIntegralAccumulator, idx)
            }
        }
        sparkMaxCache[esc] = this
    }
}