package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.Ampere
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli

class RollersComponent(hardware: RollersHardware) : Component<RollersComponent, RollersHardware, Ampere>(hardware) {
    override val fallbackController: RollersComponent.(Time) -> Ampere
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun RollersHardware.output(value: Ampere) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class RollersHardware : SubsystemHardware<RollersHardware, RollersComponent>() {
    override val priority = Priority.Medium
    override val period = 100.milli(::Second)
    override val syncThreshold = 10.milli(::Second)
    override val subsystemName = "Collector Rollers"
}
