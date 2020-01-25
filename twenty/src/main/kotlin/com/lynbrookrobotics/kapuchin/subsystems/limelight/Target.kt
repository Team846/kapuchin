package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.preferences.*

sealed class DetectedTarget(val estimate: Position) {
    class InnerGoal(estimate: Position) : DetectedTarget(estimate)
    class OuterGoal(estimate: Position) : DetectedTarget(estimate)
}