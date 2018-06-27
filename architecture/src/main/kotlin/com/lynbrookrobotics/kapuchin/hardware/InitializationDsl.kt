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
import com.lynbrookrobotics.kapuchin.subsystems.Comp
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <Value> SubsystemHardware<*, *>.hardw(nameSuffix: String = "", initialize: Named.() -> Value) = HardwareInit(this, initialize, nameSuffix = nameSuffix)

fun <Input> SubsystemHardware<*, *>.readWithComponent(read: (Time) -> TimeStamped<Input>) = WithComponentSensor(syncThreshold, read)
fun <Input> SubsystemHardware<*, *>.readWithEventLoop(read: (Time) -> TimeStamped<Input>) = WithEventLoopSensor(syncThreshold, read)
fun <Input> SubsystemHardware<*, *>.readAsynchronously(read: (Time) -> TimeStamped<Input>) = AsyncSensor(syncThreshold, priority, read)
fun <Input> SubsystemHardware<*, *>.readEagerly(read: (Time) -> TimeStamped<Input>) = EagerSensor(syncThreshold, read)

fun <Input> Comp.readWithComponent(read: (Time) -> TimeStamped<Input>) = hardware.readWithComponent(read)
fun <Input> Comp.readWithEventLoop(read: (Time) -> TimeStamped<Input>) = hardware.readWithEventLoop(read)
fun <Input> Comp.readAsynchronously(read: (Time) -> TimeStamped<Input>) = hardware.readAsynchronously(read)
fun <Input> Comp.readEagerly(read: (Time) -> TimeStamped<Input>) = hardware.readEagerly(read)

class HardwareInit<Hardw>(
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

    fun verify(that: String, f: Named.(Hardw) -> Boolean) = HardwareInit(
            parent, initialize, configure,
            { validate(it) && f(it).also { if (!it) log(Error) { that } } },
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