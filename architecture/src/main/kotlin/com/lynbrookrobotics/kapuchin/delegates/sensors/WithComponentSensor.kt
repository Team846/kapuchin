package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class WithComponentSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : Sensor<Comp, Input>(syncThreshold, read) {

    override fun provideDelegate(thisRef: Comp, prop: KProperty<*>): ReadOnlyProperty<Comp, TimeStamped<Input>> {
        thisRef.ticker.runOnTick { tickStart -> value = optimizedRead(tickStart) }

        return object : ReadOnlyProperty<Comp, TimeStamped<Input>> {
            override fun getValue(thisRef: Comp, property: KProperty<*>) = value ?: optimizedRead(currentTime)
        }
    }
}
