package com.lynbrookrobotics.kapuchin.control

import com.lynbrookrobotics.kapuchin.Quan

operator fun <Q : Quan<Q>> Q.div(that: Q): Double = this.siValue / that.siValue
fun <Q : Quan<Q>> avg(first: Q, vararg x: Q) = first.new(x.map { it.siValue }.average())
data class TwoSided<Value>(val left: Value, val right: Value)