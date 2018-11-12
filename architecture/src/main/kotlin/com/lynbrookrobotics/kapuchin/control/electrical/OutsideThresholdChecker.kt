package com.lynbrookrobotics.kapuchin.control.electrical

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time

/**
 * Checks if a value has been outside a range for too long
 *
 * Has many applications.
 * Could be used to improve logging behavior.
 * Could be used to terminate autonomous routines in case the robot is stalled.
 *
 * @authors Kunal
 * @see MotorCurrentLimiter
 *
 * @param Q type of input
 * @property safeRange range in which values are considered normal
 * @property forDuration grace period for values to be outside `safeRange` before the checker triggers
 */
class OutsideThresholdChecker<Q : Quan<Q>>(val safeRange: ClosedRange<Q>, val forDuration: Time) : (Time, Q) -> Boolean {

    private var start: Time? = null

    override fun invoke(x: Time, y: Q) =
            if (y !in safeRange) {
                if (start == null) start = x
                x - (start ?: x) > forDuration
            } else {
                start = null
                false
            }
}