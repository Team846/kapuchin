package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.CollectorPivotPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

sealed class CollectorPivotPosition(val output: Boolean) {
    object Up : CollectorPivotPosition(false)
    object Down : CollectorPivotPosition(true)
}

class CollectorPivotComponent(hardware: CollectorPivotHardware) : Component<CollectorPivotComponent, CollectorPivotHardware, CollectorPivotPosition>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: CollectorPivotComponent.(Time) -> CollectorPivotPosition = { Up }

    override fun CollectorPivotHardware.output(value: CollectorPivotPosition) {
        solenoid.set(value.output)
    }
}

class CollectorPivotHardware : SubsystemHardware<CollectorPivotHardware, CollectorPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Pivot"

    val solenoidPort by pref(0)
    val solenoid by hardw { Solenoid(solenoidPort) }
}