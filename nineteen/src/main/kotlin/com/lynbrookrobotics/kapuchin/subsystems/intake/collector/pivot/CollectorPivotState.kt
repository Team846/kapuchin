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

        fun legalRanges(): List<CollectorPivotState> {
            val (legal, illegal) = Safeties.currentState(collectorPivot = CollectorPivotState().takeIf { it == Undetermined })
                    .partition { it !in Safeties.illegalStates }
            val mappedLegal = legal.mapNotNull { decode(it) }
            val mappedIllegal = illegal.mapNotNull { decode(it) }


            return when {
                mappedLegal.isEmpty() -> mappedIllegal
                else -> mappedLegal
            }
        }

    }
}
