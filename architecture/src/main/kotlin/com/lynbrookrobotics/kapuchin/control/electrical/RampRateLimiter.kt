package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

class RampRateLimiter<Q, D>(
        lastValue: TimeStamped<Q>? = null,
        val limit: (Time) -> D
) : (Time, Q) -> Q
        where Q : Quantity<Q, *, D>,
              D : Quantity<D, Q, *> {

    private var lastStamp: Time = lastValue?.stamp ?: currentTime
    private var lastOutput: Q = lastValue?.value ?: limit(lastStamp) * 0.Second

    override operator fun invoke(stamp: Time, target: Q): Q {
        val limit = limit(stamp)

        val dt = stamp - lastStamp
        val dv = target - lastOutput
        val ramp = dv / dt

        return when {
            ramp > limit -> lastOutput + limit * dt
            ramp < -limit -> lastOutput - limit * dt
            else -> target
        }.also {
            lastOutput = it
            lastStamp = stamp
        }
    }
}