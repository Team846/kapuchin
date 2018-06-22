package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import info.kunalsheth.units.generated.Quantity

data class PidGains<Input, Integ, Deriv, Output>(
        val kP: Gain<Output, Input>,
        val kI: Gain<Output, Integ>,
        val kD: Gain<Output, Deriv>,
        val kF: Gain<Output, Input>? = null,
        val integralFalloff: Int = -1
)
        where Input : Quantity<Input, Integ, Deriv>,
              Integ : Quantity<Integ, *, Input>,
              Deriv : Quantity<Deriv, Input, *>,
              Output : Quan<Output> {
}