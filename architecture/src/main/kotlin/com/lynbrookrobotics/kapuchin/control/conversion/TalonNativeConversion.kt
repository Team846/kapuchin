package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Tick
import info.kunalsheth.units.generated.milli
import kotlin.jvm.JvmName

class TalonNativeConversion<I, Q, D, DD>(val nativeUnits: Tick, val perQuantity: Q)
        where Q : Quantity<Q, I, D>,
              I : Quantity<I, *, Q>,
              D : Quantity<D, Q, DD>,
              DD : Quantity<DD, D, *> {

    @JvmName("nativeAbsement")
    fun native(x: I) = native(x / nativeLoopTime)

    @JvmName("nativePosition")
    fun native(x: Q) = nativeUnits * (x / perQuantity)

    @JvmName("nativeVelocity")
    fun native(x: D) = native(x * nativeTimeUnit)

    @JvmName("nativeAcceleration")
    fun native(x: DD) = native(x * nativeLoopTime)

    fun realPosition(x: Tick) = perQuantity * (x / nativeUnits).siValue
    fun realVelocity(x: Tick) = realPosition(x / nativeTimeUnit.Second) / t

    companion object {
        val t = 1.Second
        val nativeTimeUnit = 100.milli(::Second)
        val nativeLoopTime = 1.milli(::Second)
    }
}