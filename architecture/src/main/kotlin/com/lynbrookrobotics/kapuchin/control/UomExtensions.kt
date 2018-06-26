package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.Quan

operator fun <Q : Quan<Q>> Q.div(that: Q): Double = this.siValue / that.siValue
fun <Q : Quan<Q>> avg(a: Q, b: Q) = (a + b) / 2
fun <Q : Quan<Q>> avg(first: Q, vararg x: Q) = first.new(
        (first.siValue + x.sumByDouble { it.siValue }) / (x.size + 1)
)

infix fun <Q : Quan<Q>> Q.minMag(that: Q) = if (this.abs < that.abs) this else that
infix fun <Q : Quan<Q>> Q.maxMag(that: Q) = if (this.abs > that.abs) this else that
infix fun <Q : Quan<Q>> Q.withToleranceOf(of: Q) = (this - of)..(this + of)