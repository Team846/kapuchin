package com.lynbrookrobotics.kapuchin.control.math.integration

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time

interface Integrator<Q, SQDT> : (Time, Q) -> SQDT
        where SQDT : Quan<SQDT>,
              Q : Quan<Q>
