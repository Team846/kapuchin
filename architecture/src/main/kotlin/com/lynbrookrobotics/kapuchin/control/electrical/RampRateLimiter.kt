package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

class RampRateLimiter<Q, D>(
        private val div: (Q, T) -> D,
        private val times: (D, T) -> Q,
        lastValue: TimeStamped<Q>? = null,
        val limit: (Time) -> D
) : (Time, Q) -> Q

        where Q : Quan<Q>,
              D : Quan<D> {

    private var lastStamp: Time = lastValue?.stamp ?: currentTime
    private var lastOutput: Q = lastValue?.value ?: times(limit(lastStamp), 0.Second)

    override operator fun invoke(stamp: Time, target: Q): Q {
        val limit = limit(stamp)

        val dt = stamp - lastStamp
        val dv = target - lastOutput
        val ramp = div(dv, dt)

        return when {
            ramp > limit -> lastOutput + times(limit, dt)
            ramp < -limit -> lastOutput - times(limit, dt)
            else -> target
        }.also {
            lastOutput = it
            lastStamp = stamp
        }
    }
}