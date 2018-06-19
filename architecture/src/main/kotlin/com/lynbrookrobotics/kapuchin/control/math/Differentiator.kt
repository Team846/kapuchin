package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Time

class Differentiator<DerivativeOfQ, Q>(private val init: DerivativeOfQ) : (Time, Q) -> DerivativeOfQ
        where DerivativeOfQ : Quantity<DerivativeOfQ, Q, *>,
              Q : Quantity<Q, *, DerivativeOfQ> {

    private lateinit var x1: Time
    private lateinit var y1: Q

    override fun invoke(x2: Time, y2: Q) =
            (if (::x1.isInitialized && ::y1.isInitialized) (y2 - y1) / (x2 - x1)
            else init)
                    .also {
                        x1 = x2
                        y1 = y2
                    }
}