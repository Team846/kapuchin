package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

abstract class Sensor<Input>(private val syncThreshold: Time, private val read: (Time) -> TimeStamped<Input>) : DelegateProvider<Comp, TimeStamped<Input>> {

    protected var value: TimeStamped<Input>? = null
    protected fun optimizedRead(currentTime: Time) = value
            ?.takeIf { currentTime - it.stamp in 0.Second..syncThreshold }
            ?: read(currentTime)
}