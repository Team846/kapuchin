package com.lynbrookrobotics.kapuchin.control.conversion

class MinOutputMap(val min: Double) : (Double) -> Double {
    override fun invoke(input: Double): Double = when {
        input > 0 -> input * (1 - min) + min
        input < 0 -> input * (1 - min) - min
        else -> 0.0
    }
}