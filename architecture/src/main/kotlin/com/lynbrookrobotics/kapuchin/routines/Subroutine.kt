package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import info.kunalsheth.units.generated.Time

class Subroutine<C, H, Output>(
        name: String,
        val component: C,
        val controller: C.(Time) -> Output,
        val isFinished: C.(Time) -> Boolean
) : Named(component, name), Routine
        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    override fun start() {
        component.controller = controller
    }

    override fun stop() {
        component.controller = component.fallbackController
    }

    override fun equals(other: Any?) = when (other) {
        is Subroutine<*, *, *> -> other.component == this.component
        else -> false
    }

    override fun hashCode() = component.hashCode()
}