package com.lynbrookrobotics.kapuchin.control.math.integration

import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

open class InfiniteIntegrator<IntegralOfQ, Q>(private val init: IntegralOfQ) : Integrator<IntegralOfQ, Q>
        where IntegralOfQ : Quantity<IntegralOfQ, *, Q>,
              Q : Quantity<Q, IntegralOfQ, *> {

    protected val zero = init.new(0.0)
    protected var sum = zero


    override fun invoke(x2: Time, y2: Q) = (sum + recentRectangle(x2, y2))
            .also { sum = it }

    private lateinit var x1: Time
    private lateinit var y1: Q
    protected fun recentRectangle(x2: Time, y2: Q) = (
            if (::x1.isInitialized && ::y1.isInitialized) {
                val height = (y1 + y2) / 2
                val width = (x2 - x1)
                height * width
            } else init
            )
            .also {
                x1 = x2
                y1 = y2
            }
}