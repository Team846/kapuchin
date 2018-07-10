package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class Subsystem1Component(hardware: Subsystem1Hardware) : Component<Subsystem1Component, Subsystem1Hardware, String>(hardware) {
    override val fallbackController: Subsystem1Component.(Time) -> String = { "fallback controller" }

    override fun Subsystem1Hardware.output(value: String) {
        log(Debug) { "output:\n$value" }
    }
}

class Subsystem1Hardware : SubsystemHardware<Subsystem1Hardware, Subsystem1Component>() {
    override val priority = Priority.RealTime
    override val period = 1.Second
    override val syncThreshold = 0.5.Second
    override val subsystemName = "Test Subsys. 1"

}