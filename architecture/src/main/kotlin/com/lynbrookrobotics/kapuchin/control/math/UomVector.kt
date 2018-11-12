package com.lynbrookrobotics.kapuchin.control.math

import info.kunalsheth.units.generated.Quan
import info.kunalsheth.units.generated.avg

interface Vector<T> {
    val x: T
    val y: T
    val z: T
}

data class UomVector<Q : Quan<Q>>(override val x: Q, override val y: Q, override val z: Q) : Vector<Q>
data class NumVector(override val x: Double, override val y: Double, override val z: Double) : Vector<Double>
data class TwoSided<out Value>(val left: Value, val right: Value) {
    constructor(bothSides: Value) : this(bothSides, bothSides)
}

val <Q : Quan<Q>> TwoSided<Q>.avg get() = avg(left, right)

operator fun <Q : Quan<Q>> TwoSided<Q>.plus(that: TwoSided<Q>) = TwoSided(
        this.left + that.left,
        this.right + that.right
)

operator fun <Q : Quan<Q>> TwoSided<Q>.minus(that: TwoSided<Q>) = TwoSided(
        this.left - that.left,
        this.right - that.right
)