package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.CollectorPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class CollectorPivotState(val output: Boolean) { Up(false), Down(true) }

class CollectorPivot(hardware: CollectorPivotHardware) : Component<CollectorPivot, CollectorPivotHardware, CollectorPivotState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: CollectorPivot.(Time) -> CollectorPivotState = { Up }

    override fun CollectorPivotHardware.output(value: CollectorPivotState) {
        solenoid.set(value.output)
    }
}

class CollectorPivotHardware : SubsystemHardware<CollectorPivotHardware, CollectorPivot>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Pivot"

    val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }
}
