package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.subsystems.Comp
import com.lynbrookrobotics.kapuchin.timing.PlatformThread
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class AsyncSensor<Input>(syncThreshold: Time, private val priority: Priority, read: (Time) -> TimeStamped<Input>) : Sensor<Comp, Input>(syncThreshold, read) {

    override fun provideDelegate(thisRef: Comp, prop: KProperty<*>): ReadOnlyProperty<Comp, TimeStamped<Input>> {
        PlatformThread(thisRef, prop.name, priority) {
            while (true) {
                val tickStart = thisRef.ticker.waitOnTick()
                value = optimizedRead(tickStart)
            }
        }

        return object : ReadOnlyProperty<Comp, TimeStamped<Input>> {
            override fun getValue(thisRef: Comp, property: KProperty<*>) = value
                    ?: optimizedRead(currentTime)
        }
    }
}