package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Tick
import info.kunalsheth.units.generated.milli
import kotlin.jvm.JvmName

class TalonNativeConversion<O, I, Q, D, DD>(
        val nativeOutputUnits: Tick, val perOutputQuantity: O,
        val nativeFeedbackUnits: Tick, val perFeedbackQuantity: Q
)
        where O : Quan<O>,
              Q : Quantity<Q, I, D>,
              I : Quantity<I, *, Q>,
              D : Quantity<D, Q, DD>,
              DD : Quantity<DD, D, *> {

    @JvmName("nativeOutput")
    fun native(x: O) = nativeOutputUnits * (x / perOutputQuantity)

    @JvmName("nativeAbsement")
    fun native(x: I) = native(x / nativeLoopTime)

    @JvmName("nativePosition")
    fun native(x: Q) = nativeFeedbackUnits * (x / perFeedbackQuantity)

    @JvmName("nativeVelocity")
    fun native(x: D) = native(x * nativeTimeUnit)

    @JvmName("nativeAcceleration")
    fun native(x: DD) = native(x * nativeLoopTime)

    fun realPosition(x: Tick) = perFeedbackQuantity * (x / nativeFeedbackUnits).siValue
    fun realVelocity(x: Tick) = realPosition(x / nativeTimeUnit.Second) / t

    companion object {
        private val t = 1.Second
        val nativeTimeUnit = 100.milli(::Second)
        val nativeLoopTime = 1.milli(::Second)
    }
}