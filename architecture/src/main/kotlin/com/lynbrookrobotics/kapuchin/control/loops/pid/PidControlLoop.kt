package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.loops.ControlLoop
import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.control.math.integration.FiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

open class PidControlLoop<Input, Integ, Deriv, Output, Gains>(
        private val gains: (Time, Input) -> Gains,
        private val target: (Time) -> Input
) : ControlLoop<Input, Output>
        where Input : Quantity<Input, Integ, Deriv>,
              Integ : Quantity<Integ, *, Input>,
              Deriv : Quantity<Deriv, Input, *>,
              Output : Quantity<Output, *, *>,
              Gains : PidGains<Input, Integ, Deriv, Output> {

    constructor(gains: Gains, target: (Time) -> Input) : this({ _, _ -> gains }, target)

    private val zero = target(currentTime) * 0
    private val derivative = Differentiator(zero / Second)
    private val integral = (gains(currentTime, zero)).run {
        if (integralFalloff > 0) FiniteIntegrator(integralFalloff, zero * Second)
        else InfiniteIntegrator(zero * Second)
    }

    var error: Input = zero
        private set

    override fun invoke(stamp: Time, current: Input): Output = gains(stamp, current).run {
        val target = target(stamp)
        error = error(target, current)

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