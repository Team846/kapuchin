package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

data class PidConfig<Input, Integ, Deriv, Output>(
    val kP: (Time, Input) -> Gain<Output, Input>,
    val kI: (Time, Input) -> Gain<Output, Integ>,
    val kD: (Time, Input) -> Gain<Output, Deriv>,
    val kF: (Time, Input) -> Gain<Output, Input>? =
            { _, _ -> null },
    val integralFalloff: Int = -1
)
        where Input : Quantity<Input, Integ, Deriv>,
              Integ : Quantity<Integ, *, Input>,
              Deriv : Quantity<Deriv, Input, *>,
              Output : Quan<Output> {

    constructor(
        kP: Gain<Output, Input>,
        kI: Gain<Output, Integ>,
        kD: Gain<Output, Deriv>,
        kF: Gain<Output, Input>? = null,
        integralFalloff: Int = -1
    ) : this(
            { _, _ -> kP },
            { _, _ -> kI },
            { _, _ -> kD },
            { _, _ -> kF },
            integralFalloff
    )
}