package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.preferences.*

data class DetectedTarget(val innerGoalPos: Position?, val outerGoalPos: Position?)
