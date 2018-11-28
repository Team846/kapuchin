package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.Delay
import info.kunalsheth.units.generated.Quan
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class FiniteStandardDeviation<Q : Quan<Q>, Q2 : Quan<Q2>>(
        private val times: (Q, Q) -> Q2,
        private val init: Q,
        bufferSize: Int
) : (Q) -> Q {

    private val delay = Delay<Q>(bufferSize).also { it(init) }

    private var sum = init
    private var sqSum = times(init, init)
    private var n = 1

    override operator fun invoke(value: Q): Q {
        sum += value
        sqSum += times(value, value)

        val delayed = delay(value)
        if (delayed == null) n++
        else {
            sum -= delayed
            sqSum -= times(delayed, delayed)
        }

        return stdev(times, n, sum, sqSum)
    }
}

class InfiniteStandardDeviation<Q : Quan<Q>, Q2 : Quan<Q2>>(
        private val times: (Q, Q) -> Q2,
        private val init: Q
) : (Q) -> Q {

    private var sum = init
    private var sqSum = times(init, init)
    private var n = 1

    override operator fun invoke(value: Q): Q {
        sum += value
        sqSum += times(value, value)
        n++

        return stdev(times, n, sum, sqSum)
    }
}

private fun <Q : Quan<Q>, Q2 : Quan<Q2>> stdev(times: (Q, Q) -> Q2, n: Int, sum: Q, sqSum: Q2): Q {
    val xBar = sum / n
    val xBarSum2 = times(xBar, sum) * 2 // 2̄xΣxᵢ
    val nxBarSq = times(xBar, xBar) * n // n̄x²

    val sigmaSq = (sqSum - xBarSum2 + nxBarSq) / (n - 1)
    val sigma = sum.new(sqrt(sigmaSq.siValue.absoluteValue))  // `.absoluteValue` because of floating point error

    return sigma
}