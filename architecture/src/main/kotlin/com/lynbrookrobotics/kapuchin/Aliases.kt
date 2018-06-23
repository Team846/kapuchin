package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.subsystems.Component
import info.kunalsheth.units.generated.Quantity

typealias Comp = Component<*, *, *>
typealias Quan<Q> = Quantity<Q, *, *>

fun <R> (() -> R).safeCall() = try {
    this()
} catch (t: Throwable) {
    null
}

private fun allNonNull(vararg all: Any?) = all.all { it != null }

infix fun <A, B> A.with(that: B) = this to that
infix fun <A, R> A?.creates(block: (A) -> R): R? = this?.let(block)

infix fun <A, B, R> Pair<A?, B?>.creates(block: (A, B) -> R): R? {
    val (a, b) = this
    return if (allNonNull(a, b)) block(a!!, b!!)
    else null
}

infix fun <A, B, C, R> Pair<Pair<A?, B?>, C?>.creates(block: (A, B, C) -> R): R? {
    val (ab, c) = this
    val (a, b) = ab
    return if (allNonNull(a, b, c)) block(a!!, b!!, c!!)
    else null
}

infix fun <A, B, C, D, R> Pair<Pair<Pair<A?, B?>, C?>, D?>.creates(block: (A, B, C, D) -> R): R? {
    val (abc, d) = this
    val (ab, c) = abc
    val (a, b) = ab
    return if (allNonNull(a, b, c, d)) block(a!!, b!!, c!!, d!!)
    else null
}

infix fun <A, B, C, D, E, R> Pair<Pair<Pair<Pair<A?, B?>, C?>, D?>, E?>.creates(block: (A, B, C, D, E) -> R): R? {
    val (abcd, e) = this
    val (abc, d) = abcd
    val (ab, c) = abc
    val (a, b) = ab
    return if (allNonNull(a, b, c, d, e)) block(a!!, b!!, c!!, d!!, e!!)
    else null
}