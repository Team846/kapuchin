package com.lynbrookrobotics.kapuchin.hardware.dsl

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.sensors.WithComponentSensor
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <Value> Named.hardw(nameSuffix: String = "", initialize: Named.() -> Value) = HardwareInit(this, initialize, nameSuffix = nameSuffix)

class HardwareInit<Value, H>(
        private val parent: H,
        private val initialize: Named.() -> Value,
        private val configure: Named.(Value) -> Unit = {},
        private val validate: Named.(Value) -> Boolean = { true },
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Value>
        where H : SubsystemHardware<H, *> {

    private var value: Value? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Value> {
        value = object : Named(parent, prop.name + nameSuffix) {}.run {
            try {
                initialize()
                        .also { configure(it) }
                        .also { if (!validate(it)) throw IllegalStateException("Initialized hardware is invalid.") }
            } catch (t: Throwable) {
                log(Error, t) { "Error during creation.\nMessage: ${t.message}\nCause: ${t.cause}" }
                throw t
            }
        }

        return object : ReadOnlyProperty<Any?, Value> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value!!
        }
    }

    fun configure(f: Named.(Value) -> Unit) = HardwareInit(parent, initialize, { configure(it); f(it) }, validate, nameSuffix)
    fun verify(f: Named.(Value) -> Boolean) = HardwareInit(parent, initialize, configure, { validate(it) && f(it) }, nameSuffix)

    fun <Input> withComponentSensor(read: Value.(Time) -> TimeStamped<Input>) = HardwareInit(
            parent, { WithComponentSensor(initialize(), read) }
    )

//    fun <C, H, T> withComponentSensor(read: H.(Time) -> TimeStamped<T>)
//            where C : Component<C, H, *>,
//                  H : SubsystemHardware<H, C> =
//            WithComponentSensor(hardware, read)
//
//    fun <C, H, T> withEventLoopSensor(read: H.(Time) -> TimeStamped<T>)
//            where C : Component<C, H, *>,
//                  H : SubsystemHardware<H, C> =
//            WithEventLoopSensor(hardware, read)
//
//    fun <C, H, T> asyncSensor(read: H.(Time) -> TimeStamped<T>)
//            where C : Component<C, H, *>,
//                  H : SubsystemHardware<H, C> =
//            AsyncSensor(hardware, read)
//
//    fun <C, H, T> eagerSensor(read: H.(Time) -> TimeStamped<T>)
//            where C : Component<C, H, *>,
//                  H : SubsystemHardware<H, C> =
//            EagerSensor(hardware, read)
}