package com.lynbrookrobotics.kapuchin.delegates.sensors

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.Hardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time

fun <C, H, T> C.withComponentSensor(read: H.(Time) -> TimeStamped<T>)
        where C : Component<C, H, *>,
              H : Hardware<H, C> =
        WithComponentSensor(hardware, read)

fun <C, H, T> C.withEventLoopSensor(read: H.(Time) -> TimeStamped<T>)
        where C : Component<C, H, *>,
              H : Hardware<H, C> =
        WithEventLoopSensor(hardware, read)

fun <C, H, T> C.asyncSensor(read: H.(Time) -> TimeStamped<T>)
        where C : Component<C, H, *>,
              H : Hardware<H, C> =
        AsyncSensor(hardware, read)

fun <C, H, T> C.eagerSensor(read: H.(Time) -> TimeStamped<T>)
        where C : Component<C, H, *>,
              H : Hardware<H, C> =
        EagerSensor(hardware, read)