package com.lynbrookrobotics.kapuchin.logging

import com.lynbrookrobotics.kapuchin.Quan
import kotlin.math.pow
import kotlin.math.round

infix fun Number.withDecimals(decimalPlaces: Int) = toDouble().let {
    val shifter = 10.0.pow(decimalPlaces)
    round(it * shifter) / shifter
}

infix fun <Q : Quan<Q>> Q.withDecimals(decimalPlaces: Int) = new(siValue withDecimals decimalPlaces)