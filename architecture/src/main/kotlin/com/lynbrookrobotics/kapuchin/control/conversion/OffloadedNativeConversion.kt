package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.div
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedPidGains
import info.kunalsheth.units.generated.*
import kotlin.jvm.JvmName

class OffloadedNativeConversion<O, I, Q, D, DD>(
        val nativeOutputUnits: Tick, val perOutputQuantity: O,
        val nativeFeedbackUnits: Tick, val perFeedbackQuantity: Q,
        val nativeTimeUnit: Time = 100.milli(::Second),
        val nativeLoopTime: Time = 1.milli(::Second)
)
        where O : Quan<O>,
              Q : Quantity<Q, I, D>,
              I : Quantity<I, *, Q>,
              D : Quantity<D, Q, DD>,
              DD : Quantity<DD, D, *> {

    @JvmName("nativeOutput")
    fun native(x: O) = (nativeOutputUnits * (x / perOutputQuantity)).Tick

    @JvmName("nativeAbsement")
    fun native(x: I) = native(x / nativeLoopTime)

    @JvmName("nativePosition")
    fun native(x: Q) = (nativeFeedbackUnits * (x / perFeedbackQuantity)).Tick

    @JvmName("nativeVelocity")
    fun native(x: D) = native(x * nativeTimeUnit)

    @JvmName("nativeAcceleration")
    fun native(x: DD) = native(x * nativeLoopTime)

    fun realPosition(x: Tick) = perFeedbackQuantity * (x / nativeFeedbackUnits).siValue
    fun realVelocity(x: Tick) = realPosition(x / nativeTimeUnit.Second) / 1.Second

    @JvmName("nativeAbsementGain")
    fun native(x: Gain<O, I>) = native(x.compensation) / native(x.forError)

    @JvmName("nativePositionGain")
    fun native(x: Gain<O, Q>) = native(x.compensation) / native(x.forError)

    @JvmName("nativeVelocityGain")
    fun native(x: Gain<O, D>) = native(x.compensation) / native(x.forError)

    @JvmName("nativeAccelerationGain")
    fun native(x: Gain<O, DD>) = native(x.compensation) / native(x.forError)

    @JvmName("nativePidPositionGains")
    fun native(x: PidGains<Q, I, D, O>) = OffloadedPidGains(
            native(x.kP), native(x.kI), native(x.kD), x.kF?.let(::native) ?: 0.0
    )

    @JvmName("nativePidVelocityGains")
    fun native(x: PidGains<D, Q, DD, O>) = OffloadedPidGains(
            native(x.kP), native(x.kI), native(x.kD), x.kF?.let(::native) ?: 0.0
    )
}