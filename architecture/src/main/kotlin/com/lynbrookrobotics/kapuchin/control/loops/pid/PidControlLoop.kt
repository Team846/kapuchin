package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.loops.ControlLoop
import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.control.math.integration.FiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import info.kunalsheth.units.generated.Quantity
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

open class PidControlLoop<Input, Integ, Deriv, Output, Config>(
        private val config: Config,
        private val target: (Time) -> Input
) : ControlLoop<Input, Output>
        where Input : Quantity<Input, Integ, Deriv>,
              Integ : Quantity<Integ, *, Input>,
              Deriv : Quantity<Deriv, Input, *>,
              Output : Quantity<Output, *, *>,
              Config : PidGains<Input, Integ, Deriv, Output> {

    private val zero = target(0.Second).new(0.0)
    private val derivative = Differentiator(zero / 1.Second)
    private val integral =
            if (config.integralFalloff > 0) FiniteIntegrator(config.integralFalloff, zero * 1.Second)
            else InfiniteIntegrator(zero * 1.Second)

    override fun invoke(stamp: Time, current: Input): Output = config.run {
        val target = target(stamp)
        val error = error(target, current)

        val deriv = derivative(stamp, error)
        val integ = integral(stamp, error)

        val output = kP(stamp, current) * error +
                kI(stamp, current) * integ +
                kD(stamp, current) * deriv

        kF(stamp, current)?.times(target)
                ?.let { it + output }
                ?: output
    }
}