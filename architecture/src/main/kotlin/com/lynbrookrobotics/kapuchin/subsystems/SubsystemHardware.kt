package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Time

abstract class SubsystemHardware<This, C> : Named by Named("override val name = ...")
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {
    abstract val priority: Priority
    abstract val period: Time
    abstract val syncThreshold: Time
    abstract val subsystemName: String
    final override val name get() = subsystemName
}