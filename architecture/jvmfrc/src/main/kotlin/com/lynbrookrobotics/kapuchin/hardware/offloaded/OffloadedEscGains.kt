package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.BaseTalon
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.CANPIDController
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
        val cache = ConcurrentHashMap<Any, OffloadedEscGains>()
    }

    private val timeoutMs = syncThreshold.milli(Second).toInt()

    fun writeTo(esc: BaseTalon, timeoutMs: Int = this.timeoutMs) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing gains to Talon${if (esc is TalonSRX) "SRX" else "FX"} ${esc.deviceID}")

            if (cached?.kP != this.kP)
                +esc.config_kP(idx, kP, timeoutMs)

            if (cached?.kI != this.kI)
                +esc.config_kI(idx, kI, timeoutMs)

            if (cached?.kD != this.kD)
                +esc.config_kD(idx, kD, timeoutMs)

            if (cached?.kF != this.kF)
                +esc.config_kF(idx, kF, timeoutMs)

            if (cached?.maxIntegralAccumulator != this.maxIntegralAccumulator)
                +esc.configMaxIntegralAccumulator(idx, maxIntegralAccumulator, timeoutMs)
        }
        cache[esc] = this
    }

    fun writeTo(esc: CANSparkMax, pidController: CANPIDController) {
        val cached = cache[esc]
        if (this != cached) {
            println("Writing gains to SparkMAX ${esc.deviceId}")

            if (cached?.kP != this.kP)
                +pidController.setP(kP)

            if (cached?.kI != this.kI)
                +pidController.setI(kI)

            if (cached?.kD != this.kD)
                +pidController.setD(kD)

            if (cached?.kF != this.kF)
                +pidController.setFF(kF)

            if (cached?.maxIntegralAccumulator != this.maxIntegralAccumulator)
                +pidController.setIMaxAccum(maxIntegralAccumulator, timeoutMs)
        }
        cache[esc] = this
    }
}