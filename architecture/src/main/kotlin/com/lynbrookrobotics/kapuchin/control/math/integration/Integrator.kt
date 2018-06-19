package com.lynbrookrobotics.kapuchin.control.math.integration

import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

interface Integrator<IntegralOfQ, Q> : (Time, Q) -> IntegralOfQ
        where IntegralOfQ : Quantity<IntegralOfQ, *, Q>,
              Q : Quantity<Q, IntegralOfQ, *>
