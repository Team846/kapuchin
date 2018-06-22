package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time

abstract class Hardware<This, C> : Named(null, "override val name = ...")
        where This : Hardware<This, C>,
              C : Component<C, This, *> {
    abstract val priority: Priority
    abstract val period: Time
    abstract val syncThreshold: Time
    abstract val subsystemName: String
    override val name get() = subsystemName
}