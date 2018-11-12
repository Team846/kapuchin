package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.VerticalDeadband
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import info.kunalsheth.units.generated.Quan

/**
 * Stores gains for PID control
 *
 * @author Kunal
 * @see PidControlLoop
 * @see Gain
 *
 * @param Input type of sensor feedback
 * @param Integ integral of sensor feedback
 * @param Deriv derivative of sensor feedback
 * @param Output type of output
 *
 * @property kP proportional gains
 * @property kI integral gains
 * @property kD derivative gians
 * @property kF feed forward term
 */
data class PidGains<Input, Integ, Deriv, Output>(
        val kP: Gain<Output, Input>,
        val kI: Gain<Output, Integ>,
        val kD: Gain<Output, Deriv>,
        val kF: Gain<Output, Input>? = null
)
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output> {
}