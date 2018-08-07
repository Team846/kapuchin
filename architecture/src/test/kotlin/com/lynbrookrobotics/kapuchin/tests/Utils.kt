package com.lynbrookrobotics.kapuchin.tests

import com.lynbrookrobotics.kapuchin.logging.withDecimals
import info.kunalsheth.units.generated.Quan

val anyInt = setOf(0, 1, 2, 373, 1024, 1492, 8397)
        .flatMap { setOf(it, -it) }
        .toSet()

val anyDouble = setOf(0.001, 0.3, 0.8, 1.0, 1.4, 3.7, 9.0, 23.9, 77.3, 1429.5)
        .flatMap { setOf(it, -it) }
        .toSet()

private const val numericalTolerance = 1E-5
private val <Q : Quan<Q>> Q.tolerance
    get() = (this.new(numericalTolerance))
            .let { (this - it)..(this + it) }

infix fun <Q : Quan<Q>> Q.`is equal to?`(that: Q) =
        assert(this in that.tolerance) {
            "Expecting ${this withDecimals 3} ≈ ${that withDecimals 3}, however ${this withDecimals 3} ≉ ${that withDecimals 3}"
        }

infix fun <T : Any?> T.`is equal to?`(that: T) =
        assert(this == that) { "Expecting $this = $that, however $this ≠ $that" }

infix fun <T : Comparable<T>> T.`is greater than?`(that: T) =
        assert(this > that) { "Expecting $this > $that, however $this ≤ $that" }

infix fun <T : Comparable<T>> T.`is greater than or equal to?`(that: T) =
        assert(this >= that) { "Expecting $this >= $that, however $this < $that" }

inline fun assert(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}