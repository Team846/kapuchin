package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

val anyInt = setOf(0, 1, 2, 373, 1024, 1492, 8397)
    .flatMap { setOf(it, -it) }
    .toSet()

val anyDouble = setOf(1E-4, 0.001, 0.3, 0.8, 1.0, 1.4, 3.7, 9.0, 23.9, 77.3, 123.4, 180.0, 191.34, 1429.5, 1E4)
    .flatMap { setOf(it, -it) }
    .toSet()

private const val numericalTolerance = 1E-5
private val <Q : Quan<Q>> Q.tolerance
    get() = (this.new(numericalTolerance))
        .let { (this - it)..(this + it) }

infix fun <Q : Quan<Q>> Q.`is equal to?`(that: Q) =
    assert(this in that.tolerance) {
        "Expecting ${this withDecimals 5} ≈ ${that withDecimals 5}, however ${this withDecimals 5} ≉ ${that withDecimals 5}"
    }

infix fun <T : Any?> T.`is equal to?`(that: T) =
    assert(this == that) { "Expecting $this = $that, however $this ≠ $that" }

infix fun <T : Comparable<T>> T.`is within?`(range: ClosedRange<T>) =
    assert(this in range) {
        val rangeText = "[${range.start}, ${range.endInclusive}]"
        "Expecting $this ∈ $rangeText, however $this ∉ $rangeText"
    }

infix fun <T : Comparable<T>> T.`is greater than?`(that: T) =
    assert(this > that) { "Expecting $this > $that, however $this ≤ $that" }

infix fun <T : Quan<T>> T.`is greater than or equal to?`(that: T) =
    assert(this >= that || this in that.tolerance) { "Expecting $this >= $that, however $this < $that" }

infix fun <T : Comparable<T>> T.`is greater than or equal to?`(that: T) =
    assert(this >= that) { "Expecting $this >= $that, however $this < $that" }

inline fun assert(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}