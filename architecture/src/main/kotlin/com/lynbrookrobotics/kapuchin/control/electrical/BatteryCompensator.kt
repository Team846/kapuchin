package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.Volt

fun Named.voltageToDutyCycle(target: Volt, vBat: Volt): Dimensionless {
    if (target > vBat) log(Warning) {
        "${target.Volt withDecimals 1} volt target cannot be reached with a ${vBat.Volt withDecimals 1} volt battery."
    }
    return target / vBat
}