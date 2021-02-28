package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop as KapuchinEventLoop

private typealias Block = suspend CoroutineScope.() -> Unit

/**
 * Represents a sequence of subsystem routines
 *
 * Choreographies run until all their child jobs complete, a child throws an exception, or they are cancelled.
 *
 * @author Kunal
 * @see Component
 * @see Routine
 *
 * @param name type of this choreography
 * @param setup function returning a choreography
 */
suspend fun startChoreo(
    name: String,
    setup: FreeSensorScope.() -> Block
) {
    val named = Named(name)
    val sensorScope = FreeSensorScope()
    try {
        named.log(Debug) { "Started $name choreography." }
        val controller = sensorScope.run(setup)
        coroutineScope { controller() }
        named.log(Debug) { "Completed $name choreography." }
    } catch (c: CancellationException) {
        named.log(Debug) { "Cancelled $name choreography.\n${c.message}" }
        throw c
    } catch (t: Throwable) {
        named.log(Error, t)
    } finally {
        sensorScope.close()
    }
}

/**
 * Utility function to create a new sequence of subsystem routines
 *
 * @receiver this subsystem's component
 * @param Time loop start time
 * @return value to write to hardware or `null` to end the routine
 */
fun choreography(controller: Block) = controller

/**
 * Create a new coroutine running all `routines` in parallel
 *
 * @param blocks collection of functions to run in parallel
 * @return parent coroutine of the running routines
 */
suspend fun runAll(vararg blocks: Block) = supervisorScope {
    blocks.forEach { launch { it() } }
}

/**
 * Create a new coroutine which suspends until the predicate is met
 *
 * @param predicate function to check if the coroutine should still be suspended
 * @return coroutine which suspends until `predicate` returns true
 */
suspend fun delayUntil(clock: Clock = KapuchinEventLoop, predicate: () -> Boolean) {
    var runOnTick: Cancel? = null

    if (!predicate()) try {
        suspendCancellableCoroutine<Unit> { cont ->
            runOnTick = clock.runOnTick {
                if (predicate()) {
                    runOnTick?.cancel()
                    cont.resume(Unit)
                }
            }
        }
    } finally {
        runOnTick?.cancel()
    }
}

/**
 * Create a new coroutine running the function while the predicate is met
 *
 * @param predicate function to check if the coroutine should still be running
 * @param block function to run
 * @return coroutine which runs until `predicate` returns false
 */
suspend fun runWhile(predicate: () -> Boolean, block: Block) = coroutineScope {
    if (predicate()) {
        val job = launch { block() }
        val delay = launch {
            delayUntil { !predicate() || !job.isActive }
            job.cancel()
        }
        job.join()
        delay.cancelAndJoin()
    }
}

/**
 * Run the given block whenever the predicate is met
 *
 * @param predicate function to check if the block should be run
 * @param block function to run
 * @return coroutine which runs code whenever `predicate` returns false
 */
suspend fun whenever(predicate: () -> Boolean, block: Block) = coroutineScope {
    while (isActive) {
        delayUntil(predicate = predicate)
        block()
    }
}

/**
 * Shortcut for a bunch of runWhiles in a whenever
 *
 * @param blocks List of pairs of a predicate and a function
 */
suspend fun runWhenever(vararg blocks: Pair<() -> Boolean, Block>) = supervisorScope {
    blocks.forEach { (p, b) ->
        launch {
            whenever(p) {
                runWhile(p, b)
            }
        }
    }
}

/**
 * Shortcut for a bunch of launches in a whenever
 *
 * @param blocks List of pairs of a predicate and a function
 */
suspend fun launchWhenever(vararg blocks: Pair<() -> Boolean, Block>) = supervisorScope {
    blocks.forEach { (p, b) ->
        launch {
            whenever(p) {
                launch {
                    b()
                }.join()
            }
        }
    }
}

/**
 * Pauses the coroutine for some time
 *
 * @param time period to delay for
 */
suspend fun delay(time: Time) =
    delay(time.milli(Second).toLong())

/**
 * Pauses the coroutine until cancelled
 *
 * @param time period to delay for
 */
suspend fun freeze() = suspendCancellableCoroutine<Unit> { }

/**
 * Cancels the given function if it takes too long
 *
 * @param time maximum time to run the function for
 * @param block function to run
 */
suspend fun withTimeout(time: Time, block: Block) =
    withTimeoutOrNull(time.milli(Second).toLong(), block)