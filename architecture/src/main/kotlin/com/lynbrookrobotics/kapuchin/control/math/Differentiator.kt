package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

class Differentiator<Q, DQDT>(
        private val div: (Q, T) -> DQDT,
        private var x1: Time,
        private var y1: Q
) : (Time, Q) -> DQDT

        where Q : Quan<Q>,
              DQDT : Quan<DQDT> {

    override fun invoke(x2: Time, y2: Q) = div(
            y2 - y1, x2 - x1
    ).also {
        x1 = x2
        y1 = y2
    }
}