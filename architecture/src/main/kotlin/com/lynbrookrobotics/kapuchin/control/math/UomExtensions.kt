package com.lynbrookrobotics.kapuchin.control

import info.kunalsheth.units.generated.*

operator fun <Q : Quan<Q>> Q.div(that: Q): Double = this.siValue / that.siValue

infix fun <Q : Quan<Q>> Q.minMag(that: Q) = if (this.abs < that.abs) this else that
infix fun <Q : Quan<Q>> Q.maxMag(that: Q) = if (this.abs > that.abs) this else that

infix fun Number.`±`(of: Number): ClosedFloatingPointRange<Double> {
    val center = toDouble()
    val range = of.toDouble()
    return center - range..center + range
}