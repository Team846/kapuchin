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
) : DelegateProvider<Any?, Value>, (String) -> Unit {

    private var value: Value? = null
    lateinit var name: String
        private set

    /**
     * HashMap of every key in the NetworkTable that corresponds to the Preference instance
     *
     * @author Andy
     */
    companion object {
        val keyMap = HashMap<String, Preference<*>>()
    }

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        name = Named(prop.name + nameSuffix, parent).name

        keyMap[name] = this

        init(name, get(name, fallback))

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value ?: get(name, fallback)
        }
    }

    /**
     * Called whenever a change is detected in the NetworkTable
     * Only retrieves data if the key of the Preference instance is equal to the key of the changed entry in the table
     *
     * @author Andy
     * @param p1 is the name of the key of the entry that was changed
     */
    override operator fun invoke(p1: String) {
        if (::name.isInitialized && name == p1) {
            value = get(name, fallback)
        }
    }
}
