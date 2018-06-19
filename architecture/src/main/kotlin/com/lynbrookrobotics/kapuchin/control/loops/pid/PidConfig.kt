package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

data class PidConfig<Input, Integ, Deriv, Output>(
        val kP: (Time, Input) -> Gain<Input, Output>,
        val kI: (Time, Input) -> Gain<Integ, Output>,
        val kD: (Time, Input) -> Gain<Deriv, Output>,
        val kF: (Time, Input) -> Gain<Input, Output>? =
                { _, _ -> null },
        val integralFalloff: Int = -1
)
        where Input : Quantity<Input, Integ, Deriv>,
              Integ : Quantity<Integ, *, Input>,
              Deriv : Quantity<Deriv, Input, *>,
              Output : Quan<Output> {

    constructor(
            kP: Gain<Input, Output>,
            kI: Gain<Integ, Output>,
            kD: Gain<Deriv, Output>,
            kF: Gain<Input, Output>? = null,
            integralFalloff: Int = -1
    ) : this(
            { _, _ -> kP },
            { _, _ -> kI },
            { _, _ -> kD },
            { _, _ -> kF },
            integralFalloff
    )
}