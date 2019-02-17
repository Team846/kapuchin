package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Initialize a preference object for a `Boolean`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: Boolean): Preference<Boolean>

/**
 * Initialize a preference object for a `Double`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: Double): Preference<Double>

/**
 * Initialize a preference object for a `Float`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: Float): Preference<Float>

/**
 * Initialize a preference object for a `Int`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: Int): Preference<Int>

/**
 * Initialize a preference object for a `Long`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: Long): Preference<Long>

/**
 * Initialize a preference object for a `String`
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun Named.pref(fallback: String): Preference<String>

/**
 * Initialize a preference object for a quantity
 *
 * @author Kunal
 *
 * @receiver owner of the preference
 * @param Q type of quantity being managed
 * @param fallback default value
 * @return new `Preference` delegate
 */
expect fun <Q : Quan<Q>> Named.pref(fallback: Number, withUnits: UomConverter<Q>): Preference<Q>

/**
 * Preference domain-specific language
 *
 * Helps organize and manage hot-configurable preferences
 *
 * @author Kunal
 * @see SubsystemHardware
 * @see Component
 * @see DelegateProvider
 * @see EventLoop
 *
 * @param Value type of preference data being managed
 */
open class Preference<Value>(
        private val parent: Named,
        private val fallback: Value,
        private val init: (String, Value) -> Unit,
        private val get: (String, Value) -> Value,
        private val registerCallback: (String, () -> Unit) -> Unit,
        private val prefNameSuffix: String = ""
) : Named, DelegateProvider<Any?, Value>, () -> Unit {

    override final lateinit var name: String
        private set

    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        name = nameLayer(parent, prop.name + prefNameSuffix)

        registerCallback(name, this)

        init(name, get(name, fallback))
        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
                    ?: get(name, fallback).also { value = it }
        }
    }

    /**
     * Called whenever a change is detected in the NetworkTable
     *
     * @author Andy
     */
    override operator fun invoke() {
        log(Level.Debug) { "updated value" }
        value = get(name, fallback)

        //If the Preference is in a PreferenceLayer, update the parent too
        if (parent is PreferenceLayer<*>) {
            parent.invoke()
        }
    }
}