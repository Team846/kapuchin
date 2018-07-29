package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import info.kunalsheth.units.generated.Quan
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.invoke
import com.lynbrookrobotics.kapuchin.control.plusOrMinus
import com.lynbrookrobotics.kapuchin.logging.Grapher
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Sensor<Input> protected constructor(internal val read: (Time) -> TimeStamped<Input>) {

    internal var value: TimeStamped<Input>? = null
    internal open fun optimizedRead(atTime: Time, syncThreshold: Time) = value
            ?.takeIf { it.stamp in atTime plusOrMinus syncThreshold }
            ?: read(atTime)

    internal fun startUpdates(f: (KProperty<*>) -> Unit) = UpdateSource(this, f)
    class UpdateSource<Input>(val forSensor: Sensor<Input>, val startUpdates: (KProperty<*>) -> Unit) {

        val withoutStamps by lazy<DelegateProvider<Any?, Input>> {
            object : DelegateProvider<Any?, Input> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, Input> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = forSensor.run {
                        value
                                ?: optimizedRead(currentTime, 0.Second)//.also { value = it } // this is buggy for eager sensors // todo FIX, it just returns the last value, however old
                    }.value
                }.also { startUpdates(prop) }
            }
        }

        val withStamps by lazy<DelegateProvider<Any?, TimeStamped<Input>>> {
            object : DelegateProvider<Any?, TimeStamped<Input>> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = forSensor.run {
                        value ?: optimizedRead(currentTime, 0.Second)//.also { value = it }
                    }
                }.also { startUpdates(prop) }
            }
        }
    }

    companion object {
        fun <Input> SubsystemHardware<*, *>.sensor(read: (Time) -> TimeStamped<Input>) = Sensor(read)
        fun <Hardw, Input> SubsystemHardware<*, *>.sensor(hardw: Hardw, read: Hardw.(Time) -> TimeStamped<Input>) = Sensor { read(hardw, it) }

        fun <QInput : Quan<QInput>> Sensor<QInput>.with(graph: Grapher<QInput>) =
                Sensor { t -> read(t).also { graph(it) } }

        fun <Input, QInput : Quan<QInput>> Sensor<Input>.with(graph: Grapher<QInput>, structure: (Input) -> QInput) =
                Sensor { t -> read(t).also { graph(it.stamp, structure(it.value)) } }
    }
}