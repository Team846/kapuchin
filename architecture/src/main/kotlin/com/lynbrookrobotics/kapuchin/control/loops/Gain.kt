package com.lynbrookrobotics.kapuchin.control.loops

import com.lynbrookrobotics.kapuchin.control.div
import info.kunalsheth.units.generated.Quan

/**
 * Represents a control gain
 *
 * @author Kunal
 * @see ControlLoop
 *
 * @param Compensation type of output
 * @param Error type of sensor feedback
 *
 * @property compensation amount of output in response to 1 `forError`
 * @property forError error resulting in `compensation` output
 */
data class Gain<Compensation, Error>(
        val compensation: Compensation,
        val forError: Error
)
        where Compensation : Quan<Compensation>,
              Error : Quan<Error> {

    operator fun times(input: Error) = compensation * (input / forError)
}