package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.WithEventLoop
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun <Value> Named.pref(nameSuffix: String = "", get: Named.() -> (() -> Value)) = PreferenceLayer(this, get, nameSuffix)

fun <C, E> Named.pref(fallbackCompensation: KProperty0<C>, fallbackForError: KProperty0<E>)
        where C : Quan<C>,
              E : Quan<E> =
        pref {
            val compensation by pref(fallbackCompensation)
            val forError by pref(fallbackForError)
            ({ Gain(compensation, forError) })
        }

class PreferenceLayer<Value>(
        private val parent: Named,
        private val construct: Named.() -> () -> Value,
        private val nameSuffix: String = ""
) : WithEventLoop, DelegateProvider<Any?, Value> {

    private lateinit var get: () -> Value
    private var value: Value? = null

    override fun update() {
        if (this::get.isInitialized) {
            value = get()
        }
    }

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        get = object : Named(prop.name + nameSuffix, parent) {}.run(construct)

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value!!
        }
    }
}