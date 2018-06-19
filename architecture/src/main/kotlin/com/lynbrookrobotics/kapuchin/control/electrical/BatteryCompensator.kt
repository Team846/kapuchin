package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.Comp
import com.lynbrookrobotics.kapuchin.logging.Level
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.Volt

fun Comp.voltageToDutyCycle(target: Volt, current: Volt): Dimensionless {
    if (target > current) log(Level.Warning) {
        "${target.Volt withDecimals 1} volt target cannot be reached with a ${current.Volt withDecimals 1} volt battery."
    }
    return target / current
}