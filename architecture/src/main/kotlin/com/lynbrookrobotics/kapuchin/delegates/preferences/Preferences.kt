package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

expect fun namePreference(thisRef: Named, prop: KProperty<*>): String

expect fun Named.pref(fallback: Boolean): Preference<Boolean>
expect fun Named.pref(fallback: Double): Preference<Double>
expect fun Named.pref(fallback: Float): Preference<Float>
expect fun Named.pref(fallback: Int): Preference<Int>
expect fun Named.pref(fallback: Long): Preference<Long>
expect fun <Q : Quan<Q>> Named.pref(fallback: KProperty0<Q>): UomPreference<Q>
fun <Value> Named.pref(nameSuffix: String = "", get: Named.() -> (() -> Value)) = PreferenceLayer(this, nameSuffix, get)

fun <C, E> Named.pref(fallbackCompensation: KProperty0<C>, fallbackForError: KProperty0<E>)
        where C : Quan<C>,
              E : Quan<E> =
        pref {
            val compensation by pref(fallbackCompensation)
            val forError by pref(fallbackForError)
            ({ Gain(compensation, forError) })
        }