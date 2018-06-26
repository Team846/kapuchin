package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.jvm.JvmName

data class TimeStamped<out Q>(val stamp: Time, val value: Q)

infix fun <Q> Q.stampWith(withTime: Time) = TimeStamped(withTime, this)
infix fun <Q> Q.stampSince(start: Time) = TimeStamped(avg(start, currentTime), this)
val <Q> Q.stampNow get() = TimeStamped(currentTime, this)
@JvmName("stampValueInvoke")
operator fun <Q, R> ((Time, Q) -> R).invoke(value: TimeStamped<Q>) = this(value.stamp, value.value)
@JvmName("valueStampInvoke")
operator fun <Q, R> ((Q, Time) -> R).invoke(value: TimeStamped<Q>) = this(value.value, value.stamp)
operator fun <Q : Quan<Q>> TimeStamped<Q>.plus(that: Q) = copy(value = this.value + that)
operator fun <Q : Quan<Q>> TimeStamped<Q>.minus(that: Q) = copy(value = this.value - that)