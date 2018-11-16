package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.V
import info.kunalsheth.units.generated.div

/**
 * Converts a target voltage to a duty cycle
 *
 * Intended for control code to compensate for system battery voltage. Improves consistency of robot behavior over time.
 *
 * @author Kunal
 *
 * @param target intended output voltage
 * @param vBat battery voltage
 * @return duty cycle
 */
fun Named.voltageToDutyCycle(target: V, vBat: V): Dimensionless {
    if (target > vBat) log(Warning) {
        "${target withDecimals 1} target cannot be reached with a ${vBat withDecimals 1} battery."
    }
    return target / vBat
}