package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Sensor<Input> private constructor(private val read: (Time) -> TimeStamped<Input>) {

    internal var value: TimeStamped<Input>? = null
    internal fun optimizedRead(atTime: Time, syncThreshold: Time) = value
            ?.takeIf { it.stamp in atTime withToleranceOf syncThreshold }
            ?: read(atTime)

    internal fun <C : Component<C, *, *>> startUpdates(f: (thisRef: C, prop: KProperty<*>) -> Unit) = UpdateSource(this, f)
    class UpdateSource<C : Component<C, *, *>, Input>(val forSensor: Sensor<Input>, val startUpdates: (C, KProperty<*>) -> Unit) {

        val withoutStamps = object : DelegateProvider<C, Input> {
            override fun provideDelegate(thisRef: C, prop: KProperty<*>) = object : ReadOnlyProperty<C, Input> {
                override fun getValue(thisRef: C, property: KProperty<*>) = forSensor.run {
                    value ?: optimizedRead(currentTime, thisRef.hardware.syncThreshold)
                }.value
            }.also { startUpdates(thisRef, prop) }
        }

        val withStamps = object : DelegateProvider<C, TimeStamped<Input>> {
            override fun provideDelegate(thisRef: C, prop: KProperty<*>) = object : ReadOnlyProperty<C, TimeStamped<Input>> {
                override fun getValue(thisRef: C, property: KProperty<*>) = forSensor.run {
                    value ?: optimizedRead(currentTime, thisRef.hardware.syncThreshold)
                }
            }.also { startUpdates(thisRef, prop) }
        }
    }

    companion object {
        fun <Input> SubsystemHardware<*, *>.sensor(read: (Time) -> TimeStamped<Input>) = Sensor(read)
    }
}