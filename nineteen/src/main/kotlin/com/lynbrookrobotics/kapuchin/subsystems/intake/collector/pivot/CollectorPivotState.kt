package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.RobotState.Companion.decode


enum class CollectorPivotState(val output: Boolean) {

    Up(false),
    Down(true),
    Undetermined(true);

    companion object {
        val states = arrayOf(CollectorPivotState.Up, CollectorPivotState.Down)
        operator fun invoke() = CollectorPivotState.Up
//                Subsystems.instance?.let {
//            it.collectorPivot?.hardware?.solenoid?.get().let {
//                if (it == null) {
//                    CollectorPivotState.Undetermined
//                } else {
//                    when (it) {
//                        CollectorPivotState.Up.output -> CollectorPivotState.Up
//                        CollectorPivotState.Down.output -> CollectorPivotState.Down
//                        else -> CollectorPivotState.Undetermined
//                    }
//                }
//            }
//        }

        fun legalRanges() = Safeties.currentState(collectorPivot = CollectorPivotState().takeIf { it == CollectorPivotState.Undetermined })
                .filter { it !in Safeties.illegalStates }
                .mapNotNull { decode(it) }

        fun init() {
            CollectorPivotState.Companion
        }

    }
}
