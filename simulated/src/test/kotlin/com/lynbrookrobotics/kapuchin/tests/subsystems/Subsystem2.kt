package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class Subsystem2Component(hardware: Subsystem2Hardware) : Component<Subsystem2Component, Subsystem2Hardware, String>(hardware) {
    override val fallbackController: Subsystem2Component.(Time) -> String = { "fallback controller" }

    override fun Subsystem2Hardware.output(value: String) {
        log(Debug) { "output:\n$value" }
    }
}

class Subsystem2Hardware : SubsystemHardware<Subsystem2Hardware, Subsystem2Component>() {
    override val priority = Priority.RealTime
    override val period = 25.milli(::Second)
    override val syncThreshold = 5.milli(::Second)
    override val subsystemName = "Test Subsys. 2"

}