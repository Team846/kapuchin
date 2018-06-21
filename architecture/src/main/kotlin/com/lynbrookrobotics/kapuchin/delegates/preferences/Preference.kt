package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Preference<Value>(
        private val thisRef: Named,
        private val fallback: Value,
        private val get: (String, Value) -> Value,
        private val nameSuffix: String = ""
) : WithEventLoop, DelegateProvider<Any?, Value> {

    private lateinit var name: String
    private var value: Value? = null

    override fun update() {
        if (this::name.isInitialized) {
            value = get(name, fallback)
        }
    }

    override fun provideDelegate(x: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        name = namePreference(thisRef, prop) + nameSuffix

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(x: Any?, property: KProperty<*>) = value ?: get(name, fallback)
        }
    }
}