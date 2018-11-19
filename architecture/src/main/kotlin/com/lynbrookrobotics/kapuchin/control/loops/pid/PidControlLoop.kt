package com.lynbrookrobotics.kapuchin.control.loops.pid

import com.lynbrookrobotics.kapuchin.control.math.differentiator
import com.lynbrookrobotics.kapuchin.control.math.finiteIntegrator
import com.lynbrookrobotics.kapuchin.control.math.infiniteIntegrator
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
fun <Input, Integ, Deriv, Output> pidControlLoop(
        div: (Input, T) -> Deriv,
        times: (Input, T) -> Integ,
        gains: (Time, Input) -> PidGains<Input, Integ, Deriv, Output>,
        integralFalloff: Int = 100,
        target: (Time) -> Input
): (Time, Input) -> Output
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output> {

    val zero = target(currentTime) * 0
    val derivative = differentiator(div, currentTime, zero)

    val integral =
            if (integralFalloff > 0) finiteIntegrator(times,
                    currentTime, zero,
                    integralFalloff
            )
            else infiniteIntegrator(times,
                    currentTime, zero
            )

    return fun(stamp: Time, current: Input): Output = gains(stamp, current).run {
        val target = target(stamp)
        val error = target - current

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

fun <Input, Integ, Deriv, Output> pidControlLoop(
        div: (Input, T) -> Deriv,
        times: (Input, T) -> Integ,
        gains: PidGains<Input, Integ, Deriv, Output>,
        integralFalloff: Int = 100,
        target: (Time) -> Input
): (Time, Input) -> Output
        where Input : Quan<Input>,
              Integ : Quan<Integ>,
              Deriv : Quan<Deriv>,
              Output : Quan<Output> =

        pidControlLoop(div, times, { _, _ -> gains }, integralFalloff, target)