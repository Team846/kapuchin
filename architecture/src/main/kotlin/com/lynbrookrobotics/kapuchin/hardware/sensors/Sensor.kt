package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.DelegateProvider
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class Sensor<Receiver, Input>(private val syncThreshold: Time, private val read: (Time) -> TimeStamped<Input>) : DelegateProvider<Receiver, Input> {

    abstract fun initValueUpdates(thisRef: Receiver, prop: KProperty<*>)
    private val valueOrRead get() = value ?: optimizedRead(currentTime)
    protected var value: TimeStamped<Input>? = null
    protected fun optimizedRead(currentTime: Time) = value
            ?.takeIf { currentTime - it.stamp in 0.Second withToleranceOf syncThreshold }
            ?: read(currentTime)

    override fun provideDelegate(thisRef: Receiver, prop: KProperty<*>): ReadOnlyProperty<Receiver, Input> {
        initValueUpdates(thisRef, prop)
        return object : ReadOnlyProperty<Receiver, Input> {
            override fun getValue(thisRef: Receiver, property: KProperty<*>) = valueOrRead.value
        }
    }

    val withTimeStamps = object : DelegateProvider<Receiver, TimeStamped<Input>> {
        override fun provideDelegate(thisRef: Receiver, prop: KProperty<*>): ReadOnlyProperty<Receiver, TimeStamped<Input>> {
            initValueUpdates(thisRef, prop)
            return object : ReadOnlyProperty<Receiver, TimeStamped<Input>> {
                override fun getValue(thisRef: Receiver, property: KProperty<*>) = valueOrRead
            }
        }
    }
}