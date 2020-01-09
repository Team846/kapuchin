package com.lynbrookrobotics.kapuchin.subsystems.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.CollectorPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorPivotComponent(hardware: CollectorPivotHardware) : Component<CollectorPivotComponent, CollectorPivotHardware, CollectorPivotState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: CollectorPivotComponent.(Time) -> CollectorPivotState = { Up }

    override fun CollectorPivotHardware.output(value: CollectorPivotState) {

        solenoid.set(value.output)

//        val legal = CollectorPivotState.legalRanges()
//
//        when {
//            !legal.any() -> Unit //log(Warning) { "No legal states found" }
//            value == CollectorPivotState.Undetermined -> log(Warning) { "Illegal collector pivot state inputted" }
//            value in legal || CollectorPivotState.Undetermined in legal -> solenoid.set(value.output)
//            else -> solenoid.set(legal.first().output)
//        }
    }
}

class CollectorPivotHardware : SubsystemHardware<CollectorPivotHardware, CollectorPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Collector Pivot"

    val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }

    val irPort = 0
}
