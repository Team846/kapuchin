package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class EagerSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : Sensor<Any?, Input>(syncThreshold, read) {
    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, TimeStamped<Input>> {
        return object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = optimizedRead(currentTime)
        }
    }
}