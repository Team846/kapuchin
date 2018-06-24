package com.lynbrookrobotics.kapuchin.hardware.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.subsystems.Comp
import info.kunalsheth.units.generated.Time
import kotlin.reflect.KProperty

open class WithComponentSensor<Input>(syncThreshold: Time, read: (Time) -> TimeStamped<Input>) : Sensor<Comp, Input>(syncThreshold, read) {
    override fun initValueUpdates(thisRef: Comp, prop: KProperty<*>) {
        thisRef.ticker.runOnTick { tickStart -> value = optimizedRead(tickStart) }
    }
}
