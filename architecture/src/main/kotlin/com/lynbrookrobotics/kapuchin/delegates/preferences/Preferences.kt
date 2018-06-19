package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.subsystems.Named
import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

expect fun namePreference(thisRef: Named, prop: KProperty<*>): String

expect fun preference(fallback: Boolean): Preference<Boolean>
expect fun preference(fallback: Double): Preference<Double>
expect fun preference(fallback: Float): Preference<Float>
expect fun preference(fallback: Int): Preference<Int>
expect fun preference(fallback: Long): Preference<Long>

expect fun <Q : Quan<Q>> preference(fallback: Double, conversion: KProperty1<Double, Q>): UomPreference<Q>
expect fun <Error : Quan<Error>, Compensation : Quan<Compensation>> preference(
         fallbackError: Double,
         errorConversion: KProperty1<Double, Error>,
         fallbackComp: Double,
         compConversion: KProperty1<Double, Compensation>
): GainPreference<Error, Compensation>