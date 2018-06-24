package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.Quan

operator fun <Q : Quan<Q>> Q.div(that: Q): Double = this.siValue / that.siValue
fun <Q : Quan<Q>> avg(first: Q, vararg x: Q) = first.new(x.map { it.siValue }.average())
infix fun <Q : Quan<Q>> Q.minMag(that: Q) = if (this.abs < that.abs) this else that
infix fun <Q : Quan<Q>> Q.maxMag(that: Q) = if (this.abs > that.abs) this else that