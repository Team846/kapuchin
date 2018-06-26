package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.subsystems.Comp
import com.lynbrookrobotics.kapuchin.timing.PlatformThread
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time
import kotlin.reflect.KProperty

class AsyncSensor<Input>(syncThreshold: Time, private val priority: Priority, read: (Time) -> TimeStamped<Input>) : Sensor<Comp, Input>(syncThreshold, read) {
    override fun initValueUpdates(thisRef: Comp, prop: KProperty<*>) {
        PlatformThread(thisRef, prop.name, priority) {
            while (true) {
                val tickStart = thisRef.ticker.waitOnTick()
                value = optimizedRead(tickStart)
            }
        }
    }
}