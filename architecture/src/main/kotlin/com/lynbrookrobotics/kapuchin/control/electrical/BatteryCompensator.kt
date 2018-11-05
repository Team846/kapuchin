package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.*

fun Named.voltageToDutyCycle(target: V, vBat: V): Dimensionless {
    if (target > vBat) log(Warning) {
        "${target withDecimals 1} target cannot be reached with a ${vBat withDecimals 1} battery."
    }
    return target / vBat
}