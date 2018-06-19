package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time

data class TimeStamped<out Q>(val stamp: Time, val value: Q)

infix fun <Q> Q.stampWith(withTime: Time) = TimeStamped(withTime, this)
infix fun <Q> Q.stampSince(start: Time) = TimeStamped(avg(start, currentTime), this)
val <Q> Q.stampNow get() = TimeStamped(currentTime, this)