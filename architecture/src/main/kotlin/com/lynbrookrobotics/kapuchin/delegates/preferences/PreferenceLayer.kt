package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PreferenceLayer<Value>(
        private val nameSuffix: String = "",
        private val construct: Named.() -> (() -> Value)
) : WithEventLoop, DelegateProvider<Named, Value> {

    private lateinit var get: () -> Value
    private var value: Value? = null

    override fun update() {
        if (this::get.isInitialized) {
            value = get()
        }
    }

    override fun provideDelegate(thisRef: Named, prop: KProperty<*>): ReadOnlyProperty<Named, Value> {
        get = object : Named {
            override val name = namePreference(thisRef, prop) + nameSuffix
        }.run(construct)

        return object : ReadOnlyProperty<Named, Value> {
            override fun getValue(thisRef: Named, property: KProperty<*>) = value!!
        }
    }
}