package com.lynbrookrobotics.kapuchin.control.math

import com.lynbrookrobotics.kapuchin.Quan

interface Vector<T> {
    val x: T
    val y: T
    val z: T
}

data class UomVector<Q : Quan<Q>>(override val x: Q, override val y: Q, override val z: Q) : Vector<Q>
data class NumVector(override val x: Double, override val y: Double, override val z: Double) : Vector<Double>