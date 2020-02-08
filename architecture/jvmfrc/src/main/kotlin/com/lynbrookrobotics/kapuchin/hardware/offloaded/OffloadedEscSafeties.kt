package com.lynbrookrobotics.kapuchin.hardware.offloaded

import com.ctre.phoenix.motorcontrol.can.TalonSRX
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
}