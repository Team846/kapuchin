package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode


sealed class CollectorPivotState(val output: Boolean) {
    object Up : CollectorPivotState(false)
    object Down : CollectorPivotState(true)
    companion object {
        val states = arrayOf(CollectorPivotState.Up, CollectorPivotState.Down)
        val pos = 4
        operator fun invoke() = Subsystems.collectorPivot.hardware.solenoid.get().let {
            when (it) {
                CollectorPivotState.Up.output -> CollectorPivotState.Up
                CollectorPivotState.Down.output -> CollectorPivotState.Down
                else -> null
            }
        }

        fun legalRanges() = Safeties.currentState(collectorPivot = null)
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it) }

    }
}


