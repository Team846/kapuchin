package com.lynbrookrobotics.kapuchin.subsystems.limelight

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import com.lynbrookrobotics.kapuchin.preferences.*

<<<<<<< HEAD
data class DetectedTarget(val inner: Position?, val outer: Position?)
//sealed class DetectedTarget(val innerEstimate: Position?, val outerEstimate: Position?) {
//    class InnerGoal(innerEstimate: Position?, outerEstimate: Position?) : DetectedTarget(innerEstimate, outerEstimate)
//    class OuterGoal(innerEstimate: Position?, outerEstimate: Position?) : DetectedTarget(innerEstimate, outerEstimate)
//}
=======
data class DetectedTarget(val innerGoalPos: Position?, val outerGoalPos: Position?)
>>>>>>> teleop2020