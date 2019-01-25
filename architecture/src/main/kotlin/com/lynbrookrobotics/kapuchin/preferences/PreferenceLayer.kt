package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.data.Gain
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.UomConverter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Initialize a preference layer for a value created by several sub-preferences
 *
 * @author Kunal
 *
 * @receiver owner of the preference layer
 * @param Value
 * @param nameSuffix storage name suffix
 * @param get function returning value creator
 * @return new `PreferenceLayer` delegate
 */
fun <Value> Named.pref(nameSuffix: String = "", get: Named.() -> (() -> Value)) = PreferenceLayer(this, get, nameSuffix)

/**
 * Initialize a preference layer for a gain value
 *
 * @author Kunal
 * @see Gain
 *
 * @receiver owner of the preference layer
 * @param C type of output
 * @param E type of sensor feedback
 * @param comp amount of output in response to 1 `forError`
 * @param compUnits `comp` units-of-measure
 * @param err error resulting in `comp` output
 * @param errUnits `err` units-of-measure
 * @return new `PreferenceLayer` delegate
 */
fun <C, E> Named.pref(comp: Number, compUnits: UomConverter<C>, err: Number, errUnits: UomConverter<E>)
        where C : Quan<C>,
              E : Quan<E> =
        pref {
            val compensation by pref(comp, compUnits)
            val forError by pref(err, errUnits)
            ({ Gain(compensation, forError) })
        }

/**
 * Preference domain-specific language
 *
 * Helps organize and manage hot-configurable preference values that are composed of several sub-preferences
 *
 * @author Kunal
 * @see SubsystemHardware
 * @see Component
 * @see DelegateProvider
 * @see EventLoop
 *
 * @param Value type of preference data being managed
 */
class PreferenceLayer<Value>(
        private val parent: Named,
        private val construct: Named.() -> () -> Value,
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Value>, () -> Unit {

    private lateinit var get: () -> Value
    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        get = Named(prop.name + nameSuffix, parent).run(construct)

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value ?: get()
        }
    }

    /**
     * Called if a Preference value is changed and that Preference is a part of this PreferenceLayer
     *
     * @author Andy
     */
    override operator fun invoke() {
        value = get()

        //If the PreferenceLayer is in another PreferenceLayer, update the parent too
        if (parent is PreferenceLayer<*>) {
            parent.invoke()
        }
    }
}