package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.loops.ControlLoop
import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.control.math.integration.FiniteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.integration.InfiniteIntegrator
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time

/**
 * Runs calculations for PID control
 *
 * Intended to be a quick-and-easy control implementation.
 *
 * @author Kunal
 * @see PidGains
 *
 * @param Input type of sensor feedback
 * @param Integ integral of sensor feedback
 * @param Deriv derivative of sensor feedback
 * @param Output type of output
 * @param Gains type of PidGains to use
 *
 * @param div UOM proof (just pass in `::div`)
 * @param times UOM proof (just pass in `::times`)
 * @param integralFalloff size of integrator buffer. If less than or equal to zero, accumulates integral error forever.
 * @param gains function returning gains
 * @param target function returning target
 */
open class PidControlLoop<Input, Integ, Deriv, Output, Gains>(
        div: (Input, T) -> Deriv,
        times: (Input, T) -> Integ,
        private val gains: (Time, Input) -> Gains,
        private val target: (Time) -> Input,
        integralFalloff: Int = 100
) : ControlLoop<Input, Output>
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output>,
              Gains : PidGains<Input, Integ, Deriv, Output> {

    constructor(div: (Input, T) -> Deriv,
                times: (Input, T) -> Integ,
                gains: Gains,
                target: (Time) -> Input,
                integralFalloff: Int = 100
    ) : this(div, times, { _, _ -> gains }, target, integralFalloff)

    private val zero = target(currentTime) * 0
    private val derivative = Differentiator(div, currentTime, zero)

    private val integral =
            if (integralFalloff > 0) FiniteIntegrator(times,
                    currentTime, zero,
                    integralFalloff
            )
            else InfiniteIntegrator(times,
                    currentTime, zero
            )

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