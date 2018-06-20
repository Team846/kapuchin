package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Preference<Value>(private val fallback: Value, private val get: (String, Value) -> Value, private val nameSuffix: String = "") : WithEventLoop, DelegateProvider<Named, Value> {

    private lateinit var name: String
    private var value: Value? = null

    override fun update() {
        if (this::name.isInitialized) {
            value = get(name, fallback)
        }
    }

    override fun provideDelegate(thisRef: Named, prop: KProperty<*>): ReadOnlyProperty<Named, Value> {
        name = namePreference(thisRef, prop) + nameSuffix

        return object : ReadOnlyProperty<Named, Value> {
            override fun getValue(thisRef: Named, property: KProperty<*>) = value ?: get(name, fallback)
        }
    }
}