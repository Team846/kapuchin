package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.Volt
import info.kunalsheth.units.generated.VoltPerSecond

class RampRateLimiter(
        val limit: VoltPerSecond,
        lastValue: TimeStamped<Volt> = 0.Volt stampWith currentTime
) : (Time, Volt) -> Volt {

    private val absLimit = limit.abs

    private var lastStamp: Time = lastValue.stamp
    private var lastOutput: Volt = lastValue.value

    override operator fun invoke(stamp: Time, target: Volt): Volt {
        val dt = stamp - lastStamp
        val dv = target - lastOutput
        val ramp = dv / dt

        return when {
            ramp > absLimit -> lastOutput + dt * absLimit
            ramp < -absLimit -> lastOutput - dt * absLimit
            else -> target
        }.also {
            lastOutput = it
            lastStamp = stamp
        }
    }
}