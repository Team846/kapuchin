package com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot

import com.lynbrookrobotics.kapuchin.*

enum class CollectorPivotState(val output: Boolean) {

    Up(false),
    Down(true);
}