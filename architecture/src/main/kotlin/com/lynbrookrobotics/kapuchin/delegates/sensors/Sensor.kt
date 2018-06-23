package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

abstract class Sensor<Receiver, Input>(private val syncThreshold: Time, private val read: (Time) -> TimeStamped<Input>) : DelegateProvider<Receiver, TimeStamped<Input>> {

    protected var value: TimeStamped<Input>? = null
    protected fun optimizedRead(currentTime: Time) = value
            ?.takeIf { currentTime - it.stamp in 0.Second..syncThreshold }
            ?: read(currentTime)
}