package com.lynbrookrobotics.kapuchin.control.math.integration

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.Time

/**
 * Represents an integration algorithm
 *
 * @author Kunal
 * @see FiniteIntegrator
 * @see InfiniteIntegrator
 *
 * @param Q type of input
 * @param SQDT integral of input
 */
interface Integrator<Q, SQDT> : (Time, Q) -> SQDT
        where SQDT : Quan<SQDT>,
              Q : Quan<Q>
