package com.lynbrookrobotics.kapuchin.control

import info.kunalsheth.units.generated.Quan

operator fun <Q : Quan<Q>> Q.div(that: Q): Double = this.siValue / that.siValue

infix fun <Q : Quan<Q>> Q.minMag(that: Q) = if (this.abs < that.abs) this else that
infix fun <Q : Quan<Q>> Q.maxMag(that: Q) = if (this.abs > that.abs) this else that

fun <Q : Quan<Q>> `±`(range: Q) = -range..range
infix fun <Q : Quan<Q>> Q.cap(range: ClosedRange<Q>) = when {
    this > range.endInclusive -> range.endInclusive
    this < range.start -> range.start
    else -> this
}

infix fun Number.`±`(of: Number): ClosedFloatingPointRange<Double> {
    val center = toDouble()
    val range = of.toDouble()
    return center - range..center + range
}