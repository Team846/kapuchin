package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.UomConverter
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
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Value> {

    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        val name = Named(prop.name + nameSuffix, parent).name

        init(name, get(name, fallback))
        EventLoop.runOnTick { value = get(name, fallback) }

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value
                    ?: get(name, fallback).also { value = it }
        }
    }
}