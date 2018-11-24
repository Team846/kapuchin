package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.Delay
import info.kunalsheth.units.generated.Quan
import kotlin.math.sqrt

class FiniteStandardDeviation<Q : Quan<Q>, Q2 : Quan<Q2>>(
        private val times: (Q, Q) -> Q2,
        private val zero: Q,
        bufferSize: Int
) : (Q) -> Q {

    private val delay = Delay<Q>(bufferSize)

    private var sum = zero
    private var sqSum = times(zero, zero)
    private var n = 0

    override operator fun invoke(value: Q): Q {
        val delayed = delay(value) ?: zero
        n += if (delayed === zero) 1 else 0

        sum += value - delayed
        sqSum += times(value, value) - times(delayed, delayed)

        return stdev(times, n, sum, sqSum)
    }
}

class InfiniteStandardDeviation<Q : Quan<Q>, Q2 : Quan<Q2>>(
        private val times: (Q, Q) -> Q2,
        private val zero: Q
) : (Q) -> Q {

    private var sum = zero
    private var sqSum = times(zero, zero)
    private var n = 0

    override operator fun invoke(value: Q): Q {
        n++
        sum += value
        sqSum += times(value, value)

        return stdev(times, n, sum, sqSum)
    }
}

private fun <Q : Quan<Q>, Q2 : Quan<Q2>> stdev(times: (Q, Q) -> Q2, n: Int, sum: Q, sqSum: Q2): Q {
    val xBar = sum / n
    val xBarSum2 = times(xBar, sum) * 2 // 2̄xΣxᵢ
    val nxBarSq = times(xBar, xBar) * n // n̄x²

    val sigmaSq = (sqSum - xBarSum2 + nxBarSq) / (n - 1)
    val sigma = sum.new(sqrt(sigmaSq.siValue))

    return sigma
}