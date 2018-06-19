package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time

abstract class Hardware<This, C> : Named
        where This : Hardware<This, C>,
              C : Component<C, This, *> {
    abstract val priority: Priority
    abstract val period: Time
    abstract val syncThreshold: Time
}