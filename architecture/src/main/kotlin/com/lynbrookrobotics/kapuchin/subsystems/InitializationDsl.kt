package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

typealias Comp = Component<*, *, *>

fun <R> Named.init(f: () -> R) = async {
    try {
        f()
    } catch (t: Throwable) {
        log(Error, t) { "Exception thrown during initialization." }
        throw t
    }
}

suspend infix fun <I1, R> ((I1) -> R).with(di1: Deferred<I1>): () -> R {
    val i1 = di1.await()
    return { this@with(i1) }
}

suspend infix fun <I1, I2, R> ((I1, I2) -> R).with(di1: Deferred<I1>): (I2) -> R {
    val i1 = di1.await()
    return { i2 -> this@with(i1, i2) }
}

suspend infix fun <I1, I2, I3, R> ((I1, I2, I3) -> R).with(di1: Deferred<I1>): (I2, I3) -> R {
    val i1 = di1.await()
    return { i2, i3 -> this@with(i1, i2, i3) }
}

suspend infix fun <I1, I2, I3, I4, R> ((I1, I2, I3, I4) -> R).with(di1: Deferred<I1>): (I2, I3, I4) -> R {
    val i1 = di1.await()
    return { i2, i3, i4 -> this@with(i1, i2, i3, i4) }
}