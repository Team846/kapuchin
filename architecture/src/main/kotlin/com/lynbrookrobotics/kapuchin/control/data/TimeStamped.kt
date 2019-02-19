package com.lynbrookrobotics.kapuchin.control.data

import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Represents data that has a time stamp
 *
 * Intended as a return type in functions which must return both a value and a timestamp. Other use cases are discouraged.
 *
 * @author Kunal
 *
 * @param T type of value
 * @property x timestamp
 * @property y value
 */
//@Deprecated(
//        message = "Try to use two separate parameters wherever possible",
//        replaceWith = ReplaceWith("x: Time, y: Q")
//)
data class TimeStamped<out T>(val x: Time, val y: T)

infix fun <Q> Q.stampWith(withTime: Time) = TimeStamped(withTime, this)
infix fun <Q> Q.stampSince(start: Time) = TimeStamped(avg(start, currentTime), this)