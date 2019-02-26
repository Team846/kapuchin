package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode


enum class CollectorPivotState(val output: Boolean) {

    Up(false),
    Down(true);

    companion object {
        val states = arrayOf(CollectorPivotState.Up, CollectorPivotState.Down)
        val pos = 4
        operator fun invoke() = Subsystems.collectorPivot.hardware.solenoid.get().let {
            when (it) {
                CollectorPivotState.Up.output -> CollectorPivotState.Up.also { println("CollectorPivotState: Up") }
                CollectorPivotState.Down.output -> CollectorPivotState.Down.also { println("CollectorPivotState: Down") }
                else -> null.also { println("CollectorPivotState: Unknown") }
            }
        }

        fun legalRanges() = Safeties.currentState(collectorPivot = null)
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it) }

        fun init() {
            CollectorPivotState.Companion
        }

    }
}
