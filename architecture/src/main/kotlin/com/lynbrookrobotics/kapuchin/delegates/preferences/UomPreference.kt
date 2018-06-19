package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class UomPreference<Value>(
        private val fallback: Double, private val uomName: String, private val conversion: (Double) -> Value,
        private val get: (String, Double) -> Double
) : WithEventLoop, DelegateProvider<Named, Value>
        where Value : Quan<Value> {

    private lateinit var name: String

    private var value: Value? = null

    override fun update() {
        if (this::name.isInitialized) {
            value = conversion(get(name, fallback))
        }
    }

    override fun provideDelegate(thisRef: Named, prop: KProperty<*>): ReadOnlyProperty<Named, Value> {
        name = "${namePreference(thisRef, prop)} ($uomName)"

        return object : ReadOnlyProperty<Named, Value> {
            override fun getValue(thisRef: Named, property: KProperty<*>) = value ?: update().let { value!! }
        }
    }
}