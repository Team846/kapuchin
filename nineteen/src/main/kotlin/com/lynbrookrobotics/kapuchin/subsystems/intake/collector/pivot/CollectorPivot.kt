package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.CollectorPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorPivotComponent(hardware: CollectorPivotHardware) : Component<CollectorPivotComponent, CollectorPivotHardware, CollectorPivotState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: CollectorPivotComponent.(Time) -> CollectorPivotState = { Up }

    override fun CollectorPivotHardware.output(value: CollectorPivotState) {
        val legal = legalRanges()

        when {
            !legal.any() -> log(Warning) { "No legal states found" }
            value in legal -> solenoid.set(value.output)
            else -> solenoid.set(legal.first().output)
        }
    }
}

class CollectorPivotHardware : SubsystemHardware<CollectorPivotHardware, CollectorPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Pivot"

    val solenoidPort by pref(1)
    val solenoid by hardw { Solenoid(solenoidPort) }
}
