package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import info.kunalsheth.units.generated.Quan
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.invoke
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.logging.Grapher
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Sensor<Input> private constructor(private val read: (Time) -> TimeStamped<Input>) {

    internal var value: TimeStamped<Input>? = null
    internal fun optimizedRead(atTime: Time, syncThreshold: Time) = value
            ?.takeIf { it.stamp in atTime withToleranceOf syncThreshold }
            ?: read(atTime)

    class UpdateSource<Input>(
            private val forSensor: Sensor<Input>,
            private val startUpdates: (Sensor<Input>) -> Unit = { _ -> },
            private val getValue: (Sensor<Input>) -> TimeStamped<Input> = {
                it.value ?: it.optimizedRead(currentTime, 0.Second)
            }
    ) {

        val withoutStamps get() = object : DelegateProvider<Any?, Input> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, Input> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = getValue(forSensor).value
                }.also { startUpdates(forSensor) }
            }

        val withStamps get() = object : DelegateProvider<Any?, TimeStamped<Input>> {
                override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = getValue(forSensor)
                }.also { startUpdates(forSensor) }
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