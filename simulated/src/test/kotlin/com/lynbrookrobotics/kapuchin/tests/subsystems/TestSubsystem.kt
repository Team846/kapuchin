package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

abstract class TC<This, H>(hardware: H) : Component<This, H, String>(hardware)
        where This : TC<This, H>,
              H : SubsystemHardware<H, This> {

    override val fallbackController: This.(Time) -> String = { "fallback controller" }

    override fun H.output(value: String) {
        println("output @ ${currentTime withDecimals 2} by thread #${Thread.currentThread().id} = $value")
    }
}

abstract class TSH<This, C>(override val subsystemName: String) : SubsystemHardware<This, C>()
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {

    override val priority = Priority.RealTime
    override val period = 1.Second
    override val syncThreshold = 0.5.Second
}