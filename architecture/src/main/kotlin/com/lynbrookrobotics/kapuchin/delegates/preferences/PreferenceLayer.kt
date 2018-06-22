package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.logging.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun <Value> Named.pref(nameSuffix: String = "", get: Named.() -> (() -> Value)) = PreferenceLayer(this, nameSuffix, get)

fun <C, E> Named.pref(fallbackCompensation: KProperty0<C>, fallbackForError: KProperty0<E>)
        where C : Quan<C>,
              E : Quan<E> =
        pref {
            val compensation by pref(fallbackCompensation)
            val forError by pref(fallbackForError)
            ({ Gain(compensation, forError) })
        }

class PreferenceLayer<Value>(
        private val thisRef: Named,
        private val nameSuffix: String = "",
        private val construct: Named.() -> (() -> Value)
) : WithEventLoop, DelegateProvider<Any?, Value> {

    private lateinit var get: () -> Value
    private var value: Value? = null

    override fun update() {
        if (this::get.isInitialized) {
            value = get()
        }
    }

    override fun provideDelegate(x: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        get = object : Named(thisRef, prop.name + nameSuffix) {}.run(construct)

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(x: Any?, property: KProperty<*>) = value!!
        }
    }
}