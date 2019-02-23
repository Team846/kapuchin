package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.CollectorPivotState.Companion.states
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.CollectorPivotState.Companion.pos
import kotlin.math.pow

sealed class CollectorPivotState(val output: Boolean) {
    object Up : CollectorPivotState(false)
    object Down : CollectorPivotState(true)
    companion object {
        val states = arrayOf(CollectorPivotState.Up, CollectorPivotState.Down)
        val pos = 2
        operator fun invoke() = Subsystems.collectorPivot.hardware.solenoid.get().let {
            when (it) {
                CollectorPivotState.Up.output -> CollectorPivotState.Up
                CollectorPivotState.Down.output -> CollectorPivotState.Down
                else -> null
            }
        }

    }
}

fun CollectorPivotState.encode(): Int {
    val index = states.indexOf(this)
    return if (index >= 0) index * 10.0.pow(pos - 1) as Int else throw Throwable("Unknown state encountered")
}

private fun CollectorPivotComponent.decode(state: RobotState): CollectorPivotState? {
    val index = state.code % (10.0.pow(pos) as Int)
    return states[index]
}


fun CollectorPivotComponent.legalRanges() = Safeties.currentState(collectorPivot = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it) }
