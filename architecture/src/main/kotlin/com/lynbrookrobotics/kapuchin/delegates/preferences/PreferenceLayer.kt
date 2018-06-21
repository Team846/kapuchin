package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PreferenceLayer<Value>(
        private val nameSuffix: String = "",
        private val get: Named.() -> Value
) : WithEventLoop, DelegateProvider<Named, Value> {

    private lateinit var named: Named
    private var value: Value? = null

    override fun update() {
        if (this::named.isInitialized) {
            value = get(named)
        }
    }

    override fun provideDelegate(thisRef: Named, prop: KProperty<*>): ReadOnlyProperty<Named, Value> {
        named = object : Named {
            override val name = namePreference(thisRef, prop) + nameSuffix
        }

        return object : ReadOnlyProperty<Named, Value> {
            override fun getValue(thisRef: Named, property: KProperty<*>) = value ?: get(named)
        }
    }
}