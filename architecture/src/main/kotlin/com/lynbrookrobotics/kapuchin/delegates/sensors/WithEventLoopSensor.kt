package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.Hardware
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class WithEventLoopSensor<C, H, T>(hardware: H, read: H.(Time) -> TimeStamped<T>) : WithEventLoop, Sensor<C, H, T>(hardware, read)
        where C : Component<C, H, *>,
              H : Hardware<H, C> {

    override fun update() {
        value = optimizedRead(currentTime)
    }

    override fun provideDelegate(thisRef: Comp, prop: KProperty<*>): ReadOnlyProperty<Comp, TimeStamped<T>> {
        return object : ReadOnlyProperty<Comp, TimeStamped<T>> {
            override fun getValue(thisRef: Comp, property: KProperty<*>) = value ?: update().let { value!! }
        }
    }
}