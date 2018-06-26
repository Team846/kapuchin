package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.timing.WithEventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Time
import kotlin.reflect.KProperty

open class WithEventLoopSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : WithEventLoop, Sensor<Any?, Input>(syncThreshold, read) {
    override fun update() {
        value = optimizedRead(currentTime)
    }

    override fun initValueUpdates(thisRef: Any?, prop: KProperty<*>) {}
}