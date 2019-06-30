package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*

//https://kotlinlang.org/docs/reference/type-safe-builders.html
interface Element {
    suspend fun run()
}

@DslMarker
annotation class TagMarker

@TagMarker
abstract class Tag : Element {
    val children = arrayListOf<Element>()

    protected fun <T : Element> initTag(tag: T, setup: T.() -> Unit) = tag
            .apply(setup)
            .also { children.add(it) }

    override suspend fun run() {
        children.forEach { it.run() }
    }


}


abstract class ScopeTag : Tag() {
    operator fun Routine<*, *, *>?.unaryPlus() {
        this?.let {
            children.add(RoutineElement(this))
        }
    }

    operator fun Element.unaryPlus() {
        children.add(this)
    }


}

class Choreo internal constructor(name: String) :
        FreeSensorScope(),
        Named by Named(name) {

    private val sensorScope = FreeSensorScope()
    var onStart: ScopeTag? = null
    var onEnd: ScopeTag? = null

    suspend fun run() {
        try {
            onStart?.let {
                log(Debug) { "Started $name start block." }
                it.run()
                log(Debug) { "Completed $name start block." }
            }
        } catch (c: CancellationException) {
            log(Debug) { "Cancelled $name end block.\n${c.message}" }
            throw c
        } catch (t: Throwable) {
            log(Error, t)
        } finally {
            try {
                onEnd?.let {
                    log(Debug) { "Started $name end block." }
                    it.run()
                    log(Debug) { "Completed $name end block." }
                }
            } catch (c: CancellationException) {
                log(Debug) { "Cancelled $name end block.\n${c.message}" }
                throw c
            } catch (t: Throwable) {
                log(Error, t)
            } finally {
                sensorScope.close()
            }
        }
    }
}

class RoutineElement(private val routine: Routine<*, *, *>) : Element {
    override suspend fun run() {
        coroutineScope { launch { routine.start() } }
    }
}

class BlockElement(private val block: suspend CoroutineScope.() -> Unit) : Element {
    override suspend fun run() {
        coroutineScope { block() }
    }
}

class Parallel internal constructor(private val supervised: Boolean) : ScopeTag() {
    override suspend fun run() {
        if (supervised) supervisorScope {
            children.forEach { launch { it.run() } }
        } else coroutineScope {
            children.forEach { launch { it.run() } }
        }
    }
}

class Sequential internal constructor() : ScopeTag()

class GlobalLaunch internal constructor() : ScopeTag() {
    override suspend fun run() {
        scope.launch { super.run() }
    }
}

class WithTimeout internal constructor(val time: Time) : ScopeTag() {
    override suspend fun run() {
        withTimeoutOrNull(time.milli(Second).toLong()) {
            super.run()
        }
    }
}

fun block(block: suspend CoroutineScope.() -> Unit) = BlockElement(block)
fun delay(time: Time) = block { delay(time.milli(Second).toLong()) }
fun freeze() = block { suspendCancellableCoroutine<Unit> { } }

fun globalLaunch(setup: GlobalLaunch.() -> Unit) = GlobalLaunch().apply(setup)
fun parallel(waitForChildrenToComplete: Boolean = false, setup: Parallel.() -> Unit) =
        Parallel(waitForChildrenToComplete).apply(setup)

fun withTimeout(time: Time, setup: WithTimeout.() -> Unit) = WithTimeout(time).apply(setup)
fun sequential(setup: Sequential.() -> Unit) = Sequential().apply(setup)
fun choreo(name: String, setup: Choreo.() -> Unit) = Choreo(name).apply(setup)