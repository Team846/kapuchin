package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class WithEventLoopSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : WithEventLoop, Sensor<Any?, Input>(syncThreshold, read) {

    override fun update() {
        value = optimizedRead(currentTime)
    }

    override fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, TimeStamped<Input>> {
        return object : ReadOnlyProperty<Any?, TimeStamped<Input>> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = value ?: update().let { value!! }
        }
    }
}