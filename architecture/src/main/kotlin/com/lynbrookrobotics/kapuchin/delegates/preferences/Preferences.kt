package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

expect fun namePreference(thisRef: Named, prop: KProperty<*>): String

expect fun preference(fallback: Boolean): Preference<Boolean>
expect fun preference(fallback: Double): Preference<Double>
expect fun preference(fallback: Float): Preference<Float>
expect fun preference(fallback: Int): Preference<Int>
expect fun preference(fallback: Long): Preference<Long>
expect fun <Q : Quan<Q>> preference(fallback: KProperty0<Q>): UomPreference<Q>
fun <Value> preference(nameSuffix: String = "", get: Named.() -> Value) = PreferenceLayer(nameSuffix, get)