package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class EagerSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : Sensor<Input>(syncThreshold, read) {
    override fun provideDelegate(thisRef: Comp, prop: KProperty<*>): ReadOnlyProperty<Comp, TimeStamped<Input>> {
        return object : ReadOnlyProperty<Comp, TimeStamped<Input>> {
            override fun getValue(thisRef: Comp, property: KProperty<*>) = optimizedRead(currentTime)
        }
    }
}