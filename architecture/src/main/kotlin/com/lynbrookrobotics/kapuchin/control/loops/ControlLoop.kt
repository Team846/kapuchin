package com.lynbrookrobotics.kapuchin.control.loops

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time

/**
 * Represents a control algorithm
 *
 * @author Kunal
 * @see Gain
 *
 * @param Input type of sensor feedback
 * @param Output type of output
 */
interface ControlLoop<Input, Output> : (Time, Input) -> Output
        where Input : Quan<Input>,
              Output : Quan<Output> {

    fun error(target: Input, current: Input) = target - current
}