package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import info.kunalsheth.units.generated.Time
import kotlin.reflect.KProperty

open class EagerSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : Sensor<Any?, Input>(syncThreshold, read) {
    override fun initValueUpdates(thisRef: Any?, prop: KProperty<*>) {}
}