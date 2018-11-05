package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.avg
import kotlin.jvm.JvmName

@Deprecated(
        message = "Try to use two separate parameters wherever possible",
        replaceWith = ReplaceWith("stamp: Time, value: Q")
)
data class TimeStamped<out Q>(val stamp: Time, val value: Q)

infix fun <Q> Q.stampWith(withTime: Time) = TimeStamped(withTime, this)
infix fun <Q> Q.stampSince(start: Time) = TimeStamped(avg(start, currentTime), this)