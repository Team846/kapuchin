package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

object Field : Named by Named("Field") {
    val targetDiameter by pref(30, Inch) // "diameter" of the inscribed circle of the outer goal
    val innerGoalDepth by pref(29.25, Inch) // Distance between outer and inner goal
    val targetHeight by pref(98.25, Inch) // height from floor to center of outer goal
    val ballMass by pref(0.141748, Kilogram)
    val ballDiameter by pref(7, Inch)
}