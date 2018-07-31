package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.DelegateProvider
import info.kunalsheth.units.generated.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import info.kunalsheth.units.generated.UomConverter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <Value> Named.pref(nameSuffix: String = "", get: Named.() -> (() -> Value)) = PreferenceLayer(this, get, nameSuffix)

fun <C, E> Named.pref(comp: Number, compUnits: UomConverter<C>, err: Number, errUnits: UomConverter<E>)
        where C : Quan<C>,
              E : Quan<E> =
        pref {
            val compensation by pref(comp, compUnits)
            val forError by pref(err, errUnits)
            ({ Gain(compensation, forError) })
        }

class PreferenceLayer<Value>(
        private val parent: Named,
        private val construct: Named.() -> () -> Value,
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Value> {

    private lateinit var get: () -> Value
    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        get = object : Named(prop.name + nameSuffix, parent) {}.run(construct)
        EventLoop.runOnTick {
            if (this::get.isInitialized) {
                value = get()
            }
        }

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value ?: get()
        }
    }
}