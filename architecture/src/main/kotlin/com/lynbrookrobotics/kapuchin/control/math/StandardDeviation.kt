package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import kotlin.math.absoluteValue
import kotlin.math.sqrt

/**
 * Calculates the standard deviation
 *
 * Calculates the standard deviation of all values since creation
 *
 * @author Kunal
 * @see finiteStdev
 *
 * @param Q type of input
 * @param Q2 type of input squared
 *
 * @param times UOM proof (just pass in `::times`)
 * @param init first value
 */
fun <Q : Quan<Q>, Q2 : Quan<Q2>> infiniteStdev(
        times: (Q, Q) -> Q2,
        init: Q
): (Q) -> Q {
    var sum = init
    var sqSum = times(init, init)
    var n = 1

    return fun(value: Q): Q {
        sum += value
        sqSum += times(value, value)
        n++

        return stdev(times, n, sum, sqSum)
    }
}

/**
 * Calculates the standard deviation
 *
 * Calculates the standard deviation of the last `falloff` values
 *
 * @author Kunal
 * @see infiniteStdev
 *
 * @param Q type of input
 * @param Q2 type of input squared
 *
 * @param times UOM proof (just pass in `::times`)
 * @param init first value
 * @param falloff number of values to look back when calculating
 */
fun <Q : Quan<Q>, Q2 : Quan<Q2>> finiteStdev(
        times: (Q, Q) -> Q2,
        init: Q,
        falloff: Int
): (Q) -> Q {
    val delay = delay<Q>(falloff).also { it(init) }

    var sum = init
    var sqSum = times(init, init)
    var n = 1

    return fun(value: Q): Q {
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

private fun <Q : Quan<Q>, Q2 : Quan<Q2>> stdev(times: (Q, Q) -> Q2, n: Int, sum: Q, sqSum: Q2): Q {
    val xBar = sum / n
    val xBarSum2 = times(xBar, sum) * 2 // 2̄xΣxᵢ
    val nxBarSq = times(xBar, xBar) * n // n̄x²

    val sigmaSq = (sqSum - xBarSum2 + nxBarSq) / (n - 1)
    val sigma = sum.new(sqrt(sigmaSq.siValue.absoluteValue))  // `.absoluteValue` because of floating point error

    return sigma
}