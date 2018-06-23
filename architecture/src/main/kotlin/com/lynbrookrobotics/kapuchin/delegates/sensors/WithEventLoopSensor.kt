package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class WithEventLoopSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : WithEventLoop, Sensor<Input>(syncThreshold, read) {

    override fun update() {
        value = optimizedRead(currentTime)
    }

    override fun provideDelegate(thisRef: Comp, prop: KProperty<*>): ReadOnlyProperty<Comp, TimeStamped<Input>> {
        return object : ReadOnlyProperty<Comp, TimeStamped<Input>> {
            override fun getValue(thisRef: Comp, property: KProperty<*>) = value ?: update().let { value!! }
        }
    }
}