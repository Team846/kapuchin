package com.lynbrookrobotics.kapuchin.control.conversion

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.math.*
import kotlin.jvm.JvmName

/**
 * CAN electronic speed controller sensor conversion utility
 *
 * Utility functions for converting between offloaded sensor feedback values and actual units.
 * Intended for TalonSRXs. Please look at the TalonSRX software manual for more information.
 *
 * @author Kunal
 * @see EncoderConversion
 * @see GearTrain
 *
 * @param O type of output
 * @param I integral of sensor input
 * @param Q type of sensor input
 * @param D derivative of sensor input
 * @param DD second derivative of sensor input
 *
 * @param d2 UOM proof (just pass in `::p`)
 * @param d1 UOM proof (just pass in `::p`)
 * @param t1 UOM proof (just pass in `::p`)
 * @param t2 UOM proof (just pass in `::p`)
 *
 * @property nativeOutputUnits ESC API value corresponding to 1 `perOutputQuantity`
 * @property perOutputQuantity output corresponding to `nativeOutputUnits` value
 *
 * @property nativeFeedbackUnits ESC API value corresponding to 1 `perFeedbackQuantity`
 * @property perFeedbackQuantity sensor input corresponding to `nativeFeedbackUnits` value
 *
 * @property feedbackZero sensor position that maps to zero
 *
 * @property nativeTimeUnit denominator used in ESC velocity measurements
 * @property nativeRateUnit factor used in ESC derivative and integral calculations
 */
class OffloadedNativeConversion<O, I, Q, D, DD>(
        private val d2: (I, `÷`, T) -> Q,
        private val d1: (Q, `÷`, T) -> D,
        private val t1: (D, `*`, T) -> Q,
        private val t2: (DD, `*`, T) -> D,
        val nativeOutputUnits: Int, val perOutputQuantity: O,
        val nativeFeedbackUnits: Int, val perFeedbackQuantity: Q,
        val feedbackZero: Q = perFeedbackQuantity * 0,
        val nativeTimeUnit: Time = 100.milli(Second),
        val nativeRateUnit: Time = 1.Second
)
        where O : Quan<O>,
              Q : Quan<Q>,
              I : Quan<I>,
              D : Quan<D>,
              DD : Quan<DD> {

    private fun convert(x: Q) = x * nativeFeedbackUnits / perFeedbackQuantity
    private fun convert(x: Number) = perFeedbackQuantity / nativeFeedbackUnits * x

    @JvmName("nativeOutput")
    fun native(x: O) = x * nativeOutputUnits / perOutputQuantity

    @JvmName("nativeAbsement")
    fun native(x: I) = convert(d2(x, `÷`, nativeRateUnit))

    @JvmName("nativePosition")
    fun native(x: Q) = convert(x + feedbackZero)

    @JvmName("nativeVelocity")
    fun native(x: D) = convert(t1(x, `*`, nativeTimeUnit))

    @JvmName("nativeAcceleration")
    fun native(x: DD) = native(t2(x, `*`, nativeRateUnit))

    fun realPosition(x: Number) = convert(x) - feedbackZero
    fun realVelocity(x: Number) = d1(convert(x), `÷`, nativeTimeUnit)

    @JvmName("nativeAbsementGain")
    fun native(x: Gain<O, I>) = native(x.compensation) / native(x.forError)

    @JvmName("nativePositionGain")
    fun native(x: Gain<O, Q>) = native(x.compensation) / convert(x.forError)

    @JvmName("nativeVelocityGain")
    fun native(x: Gain<O, D>) = native(x.compensation) / native(x.forError)

    @JvmName("nativeAccelerationGain")
    fun native(x: Gain<O, DD>) = native(x.compensation) / native(x.forError)
}