package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.loops.Gain
import info.kunalsheth.units.generated.Quan

data class PidGains<Input, Integ, Deriv, Output>(
        val kP: Gain<Output, Input>,
        val kI: Gain<Output, Integ>,
        val kD: Gain<Output, Deriv>,
        val kF: Gain<Output, Input>? = null,
        val integralFalloff: Int = 500
)
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output> {
}