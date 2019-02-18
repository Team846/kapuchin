package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.CollectorPivotPosition.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class CollectorPivotPosition {
    Up, Down
}

class CollectorPivotComponent(hardware: CollectorPivotHardware) : Component<CollectorPivotComponent, CollectorPivotHardware, CollectorPivotPosition>(hardware) {

    override val fallbackController: CollectorPivotComponent.(Time) -> CollectorPivotPosition = { CollectorPivotPosition.Up }

    override fun CollectorPivotHardware.output(value: CollectorPivotPosition) {
        when (value) {
            Up -> hardware.leftSolenoid.set(true)
            Down -> hardware.rightSolenoid.set(false)
        }
    }

}

class CollectorPivotHardware : SubsystemHardware<CollectorPivotHardware, CollectorPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 50.milli(Second)
    override val name: String = "Collector Pivot"

    val leftSolenoidPort by pref(0)
    val leftSolenoid by hardw { Solenoid(leftSolenoidPort) }

    val rightSolenoidPort by pref(1)
    val rightSolenoid by hardw { Solenoid(rightSolenoidPort) }
}