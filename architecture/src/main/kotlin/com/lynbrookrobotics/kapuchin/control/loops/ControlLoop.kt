package com.lynbrookrobotics.kapuchin.control.loops

import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

interface ControlLoop<Input, Output> : (Time, Input) -> Output
        where Input : Quantity<Input, *, *>,
              Output : Quantity<Output, *, *> {

    fun error(target: Input, current: Input) = target - current
}