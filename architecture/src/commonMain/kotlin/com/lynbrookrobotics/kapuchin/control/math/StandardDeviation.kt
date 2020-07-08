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
 * @param p UOM proof (just pass in `::p`)
 * @param init first value
 */
fun <Q : Quan<Q>, Q2 : Quan<Q2>> infiniteStdev(
        p: (Q, `*`, Q) -> Q2,
        init: Q
): (Q) -> Q {
    var sum = init
    var sqSum = p(init, `*`, init)
    var n = 1

    return fun(value: Q): Q {
        sum += value
        sqSum += p(value, `*`, value)
        n++

        return stdev(p, n, sum, sqSum)
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
 * @param p UOM proof (just pass in `::p`)
 * @param init first value
 * @param falloff number of values to look back when calculating
 */
fun <Q : Quan<Q>, Q2 : Quan<Q2>> finiteStdev(
        p: (Q, `*`, Q) -> Q2,
        init: Q,
        falloff: Int
): (Q) -> Q {
    val delay = delay<Q>(falloff).also { it(init) }

    var sum = init
    var sqSum = p(init, `*`, init)
    var n = 1

    return fun(value: Q): Q {
        sum += value
        sqSum += p(value, `*`, value)

        val delayed = delay(value)
        if (delayed == null) n++
        else {
            sum -= delayed
            sqSum -= p(delayed, `*`, delayed)
        }

        return stdev(p, n, sum, sqSum)
    }
}

private fun <Q : Quan<Q>, Q2 : Quan<Q2>> stdev(p: (Q, `*`, Q) -> Q2, n: Int, sum: Q, sqSum: Q2): Q {
    val xBar = sum / n
    val xBarSum2 = p(xBar, `*`, sum) * 2 // 2̄xΣxᵢ
    val nxBarSq = p(xBar, `*`, xBar) * n // n̄x²

    val sigmaSq = (sqSum - xBarSum2 + nxBarSq) / (n - 1)
    val sigma = sum.new(sqrt(sigmaSq.siValue.absoluteValue))  // `.absoluteValue` because of floating point error

    return sigma
}