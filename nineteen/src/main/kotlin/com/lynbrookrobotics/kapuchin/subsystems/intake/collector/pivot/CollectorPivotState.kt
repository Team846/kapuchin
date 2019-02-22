package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*

val collectorPivotStates = arrayOf(CollectorPivotPosition.Up, CollectorPivotPosition.Down)
fun CollectorPivotState() = Subsystems.collectorPivot.hardware.solenoid.get().let {
    when (it) {
        CollectorPivotPosition.Up.output -> CollectorPivotPosition.Up
        CollectorPivotPosition.Down.output -> CollectorPivotPosition.Down
        else -> null
    }
}

private fun CollectorPivotComponent.decode(state: RobotState): CollectorPivotPosition? {
    val collectorSliderCode = state.code and CollectorPivotPosition.collectorPivotQueryCode
    return when (collectorSliderCode) {
        0b00_000_00_1_0 -> CollectorPivotPosition.Up
        0b00_00_000_0_0 -> CollectorPivotPosition.Down
        else -> null
    }
}

fun CollectorPivotComponent.legalRanges() = Safeties.currentState(collectorPivot = null)
        .filter { it !in Safeties.illegalStates }
        .mapNotNull { decode(it) }