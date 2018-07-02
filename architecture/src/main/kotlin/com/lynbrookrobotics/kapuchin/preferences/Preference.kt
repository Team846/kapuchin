package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.nameLayer
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

expect fun Named.pref(fallback: Boolean): Preference<Boolean>
expect fun Named.pref(fallback: Double): Preference<Double>
expect fun Named.pref(fallback: Float): Preference<Float>
expect fun Named.pref(fallback: Int): Preference<Int>
expect fun Named.pref(fallback: Long): Preference<Long>

open class Preference<Value>(
        private val parent: Named,
        private val fallback: Value,
        private val get: (String, Value) -> Value,
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Value> {

    private lateinit var name: String
    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        name = nameLayer(parent, prop.name) + nameSuffix
        EventLoop.runOnTick { value = get(name, fallback) }

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value ?: get(name, fallback)
        }
    }
}