package com.lynbrookrobotics.kapuchin.control.electrical

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*

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
fun Named.voltageToDutyCycle(target: V, vBat: V, logLowVoltage: Boolean = true): Dimensionless {
    if (target > vBat && logLowVoltage) log(Warning) {
        "${target withDecimals 1} target cannot be reached with a ${vBat withDecimals 1} battery."
    }
    return target / vBat
}