package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume

internal typealias Block = suspend CoroutineScope.() -> Unit

/**
 * Create a new coroutine running all blocks in parallel.
 * The coroutine ends when all blocks are complete.
 *
 * @param blocks list of functions to run in parallel.
 */
suspend fun runAll(vararg blocks: Block): Unit = supervisorScope {
    blocks.forEach { launch { it() } }
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
 * Shortcut for a bunch of runWhiles in a whenever
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