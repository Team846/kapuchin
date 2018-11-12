package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.loops.ControlLoop
import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.control.math.integration.FiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

open class PidControlLoop<Input, Integ, Deriv, Output, Gains>(
        deriv: (Input, T) -> Deriv,
        integ: (Input, T) -> Integ,
        private val gains: (Time, Input) -> Gains,
        private val target: (Time) -> Input
) : ControlLoop<Input, Output>
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output>,
              Gains : PidGains<Input, Integ, Deriv, Output> {

    constructor(deriv: (Input, T) -> Deriv,
                integ: (Input, T) -> Integ,
                gains: Gains,
                target: (Time) -> Input
    ) : this(deriv, integ, { _, _ -> gains }, target)

    private val zero = target(currentTime) * 0
    private val derivative = Differentiator(deriv, currentTime, zero)

    private val integral = (gains(currentTime, zero)).run {
        if (integralFalloff > 0) FiniteIntegrator(integ,
                currentTime, zero,
                integralFalloff
        )
        else InfiniteIntegrator(integ,
                currentTime, zero
        )
    }

    override fun invoke(stamp: Time, current: Input): Output = gains(stamp, current).run {
        val target = target(stamp)
        val error = error(target, current)

        val deriv = derivative(stamp, error)
        val integ = integral(stamp, error)

        val output = kP * error +
                kI * integ +
                kD * deriv

        kF?.times(target)
                ?.let { it + output }
                ?: output
    }
}