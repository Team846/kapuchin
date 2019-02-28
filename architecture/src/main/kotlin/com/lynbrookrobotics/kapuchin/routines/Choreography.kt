package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume

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
suspend fun runAll(vararg blocks: Block) = coroutineScope {
    supervisorScope {
        blocks.forEach { launch { it() } }
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

        var runOnTick: Cancel? = null
        runOnTick = com.lynbrookrobotics.kapuchin.timing.clock.EventLoop.runOnTick {
            if (!predicate()) {
                runOnTick?.cancel()
                job.cancel()
            }
        }

        job.join()
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
    var cont: CancellableContinuation<Unit>? = null

    val runOnTick = com.lynbrookrobotics.kapuchin.timing.clock.EventLoop.runOnTick {
        if (predicate() && cont?.isActive == true) {
            try {
                cont?.resume(Unit)
            } catch (e: IllegalStateException) {
            }
        }
    }

    try {
        while (isActive) {
            suspendCancellableCoroutine<Unit> { cont = it }
            block()
        }
    } finally {
        runOnTick.cancel()
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