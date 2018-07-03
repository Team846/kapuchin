package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Sensor<Input> private constructor(private val read: (Time) -> TimeStamped<Input>) {

    internal var value: TimeStamped<Input>? = null
    internal fun optimizedRead(atTime: Time, syncThreshold: Time) = value
            ?.takeIf { it.stamp in atTime withToleranceOf syncThreshold }
            ?: read(atTime)

    internal fun startUpdates(f: (KProperty<*>) -> Unit) = UpdateSource(this, f)
    class UpdateSource<Input>(val forSensor: Sensor<Input>, val startUpdates: (KProperty<*>) -> Unit) {

        val withoutStamps = object : DelegateProvider<Any?, Input> {
            override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, Input> {
                override fun getValue(thisRef: Any?, property: KProperty<*>) = forSensor.run {
                    value ?: optimizedRead(currentTime, 0.Second)
                }.value
            }.also { startUpdates(prop) }
        }

        val withStamps = object : DelegateProvider<Any?, TimeStamped<Input>> {
            override fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
                override fun getValue(thisRef: Any?, property: KProperty<*>) = forSensor.run {
                    value ?: optimizedRead(currentTime, 0.Second)
                }
            }.also { startUpdates(prop) }
        }
    }

    companion object {
        fun <Input> SubsystemHardware<*, *>.sensor(read: (Time) -> TimeStamped<Input>) = Sensor(read)
        fun <Hardw, Input> SubsystemHardware<*, *>.sensor(hardw: Hardw, read: Hardw.(Time) -> TimeStamped<Input>) = Sensor { read(hardw, it) }
    }
}