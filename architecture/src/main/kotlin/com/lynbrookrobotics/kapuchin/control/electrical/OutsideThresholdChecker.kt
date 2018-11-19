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
 * @author Kunal
 * @see motorCurrentLimiter
 *
 * @param Q type of input
 * @param safeRange range in which values are considered normal
 * @param forDuration grace period for values to be outside `safeRange` before the checker triggers
 */
fun <Q : Quan<Q>> outsideThresholdChecker(safeRange: ClosedRange<Q>, forDuration: Time): (Time, Q) -> Boolean {

    var start: Time? = null

    return fun(x: Time, y: Q) =
            if (y !in safeRange) {
                if (start == null) start = x
                x - (start ?: x) > forDuration
            } else {
                start = null
                false
            }
}