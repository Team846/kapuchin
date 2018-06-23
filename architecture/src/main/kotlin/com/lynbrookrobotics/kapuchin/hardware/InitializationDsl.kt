package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.hardware.sensors.AsyncSensor
import com.lynbrookrobotics.kapuchin.hardware.sensors.EagerSensor
import com.lynbrookrobotics.kapuchin.hardware.sensors.WithComponentSensor
import com.lynbrookrobotics.kapuchin.hardware.sensors.WithEventLoopSensor
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <Value> SubsystemHardware<*, *>.hardw(nameSuffix: String = "", initialize: Named.() -> Value) = HardwareInit(this, initialize, nameSuffix = nameSuffix)

class HardwareInit<Hardw>(
        private val parent: SubsystemHardware<*, *>,
        private val initialize: Named.() -> Hardw,
        private val configure: Named.(Hardw) -> Unit = {},
        private val validate: Named.(Hardw) -> Boolean = { true },
        private val nameSuffix: String = ""
) : DelegateProvider<Any?, Hardw> {

    private var value: Hardw? = null

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Hardw> {
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

        return object : ReadOnlyProperty<Any?, Hardw> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value!!
        }
    }

    fun configure(f: Named.(Hardw) -> Unit) = HardwareInit(
            parent, initialize, { configure(it); f(it) }, validate, nameSuffix
    )

    fun verify(message: String, f: Named.(Hardw) -> Boolean) = HardwareInit(
            parent, initialize, configure,
            { validate(it) && f(it).also { if (!it) log(Error) { message } } },
            nameSuffix
    )

    fun <Input> readWithComponent(
            syncThreshold: Time = parent.syncThreshold,
            read: Hardw.(Time) -> TimeStamped<Input>
    ) = HardwareInit(
            parent,
            { val hardw = initialize(); WithComponentSensor(syncThreshold) { hardw.read(it) } },
            nameSuffix = nameSuffix
    )

    fun <Input> readWithEventLoop(
            syncThreshold: Time = parent.syncThreshold,
            read: Hardw.(Time) -> TimeStamped<Input>
    ) = HardwareInit(
            parent,
            { val hardw = initialize(); WithEventLoopSensor(syncThreshold) { hardw.read(it) } },
            nameSuffix = nameSuffix
    )

    fun <Input> readAsynchronously(
            syncThreshold: Time = parent.syncThreshold,
            priority: Priority = parent.priority,
            read: Hardw.(Time) -> TimeStamped<Input>
    ) = HardwareInit(
            parent,
            { val hardw = initialize(); AsyncSensor(syncThreshold, priority) { hardw.read(it) } },
            nameSuffix = nameSuffix
    )

    fun <Input> readEagerly(
            syncThreshold: Time = parent.syncThreshold,
            read: Hardw.(Time) -> TimeStamped<Input>
    ) = HardwareInit(
            parent,
            { val hardw = initialize(); EagerSensor(syncThreshold) { hardw.read(it) } },
            nameSuffix = nameSuffix
    )
}