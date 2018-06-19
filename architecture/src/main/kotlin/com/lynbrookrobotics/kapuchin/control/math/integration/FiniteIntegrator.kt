package com.lynbrookrobotics.kapuchin.control.math.integration

import com.lynbrookrobotics.kapuchin.control.math.Delay
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

class FiniteIntegrator<IntegralOfQ, Q>(val falloff: Int, init: IntegralOfQ) : InfiniteIntegrator<IntegralOfQ, Q>(init)
        where IntegralOfQ : Quantity<IntegralOfQ, *, Q>,
              Q : Quantity<Q, IntegralOfQ, *> {

    private val delayed = Delay<IntegralOfQ>(falloff)

    override fun invoke(x2: Time, y2: Q): IntegralOfQ {
        val rectangle = recentRectangle(x2, y2)
        val delayed = delayed(rectangle) ?: zero

        return (sum + rectangle - delayed)
                .also { sum = it }
    }
}