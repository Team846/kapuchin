package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class HardwareInit<Hardw> private constructor(
        private val parent: SubsystemHardware<*, *>,
        private val initialize: Named.() -> Hardw,
        private val configure: Named.(Hardw) -> Unit = {},
        private val validate: Named.(Hardw) -> Boolean = { true },
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Hardw> {

    private var value: Hardw? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Hardw> {
        value = object : Named(prop.name + nameSuffix, parent) {}.run {
            try {
                initialize()
                        .also { configure(it) }
                        .also { if (!validate(it)) error("Initialized hardware is invalid.") }
            } catch (t: Throwable) {
                log(Error, t) { "Error during creation.\nMessage: ${t.message}\nCause: ${t.cause}" }
                throw t
            }
        }

        return object : ReadOnlyProperty<Any?, Hardw> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value!!
        }
    }

    fun configure(f: Named.(Hardw) -> Unit) = HardwareInit(
            parent, initialize, { configure(it); f(it) }, validate, nameSuffix
    )

    fun verify(that: String, f: Named.(Hardw) -> Boolean) = HardwareInit(
            parent, initialize, configure,
            { validate(it) && f(it).also { if (!it) log(Error) { that } } },
            nameSuffix
    )

    fun <Input> sensor(read: Hardw.(Time) -> TimeStamped<Input>) = HardwareInit(
            parent,
            { val hardw = initialize(); parent.sensor(hardw, read) },
            nameSuffix = nameSuffix
    )

    companion object {
        fun <Value> SubsystemHardware<*, *>.hardw(nameSuffix: String = "", initialize: Named.() -> Value) = HardwareInit(this, initialize, nameSuffix = nameSuffix)
    }
}