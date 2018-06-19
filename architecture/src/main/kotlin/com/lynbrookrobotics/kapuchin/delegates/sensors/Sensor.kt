package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.Hardware
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

abstract class Sensor<C, H, T>(private val hardware: H, private val read: H.(Time) -> TimeStamped<T>) : DelegateProvider<Comp, TimeStamped<T>>
        where C : Component<C, H, *>,
              H : Hardware<H, C> {

    protected var value: TimeStamped<T>? = null
    protected fun optimizedRead(currentTime: Time) = value
            ?.takeIf { currentTime - it.stamp in 0.Second..hardware.syncThreshold }
            ?: read(hardware, currentTime)
}