package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume

internal typealias Block = suspend CoroutineScope.() -> Unit

/**
 * Runs a block, but cancels it once its predicate returns false.
 *
 * @param predicate function to check if the coroutine should still be running.
 * @param block function to run
 */
suspend fun runWhile(predicate: () -> Boolean, block: Block) = coroutineScope {
    if (predicate()) {
        val job = launch { block() }

        var runOnTick: Cancel? = null
        @Suppress("RemoveRedundantQualifierName")
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
 * Creates a new coroutine running blocks in parallel.
 * The coroutine ends when all blocks are complete.
 *
 * @param blocks list of functions to run in parallel.
 */
suspend fun runAll(vararg blocks: Block) = supervisorScope {
    blocks.forEach {
        launch {
            it()
        }
    }
}

/**
 * Pauses the coroutine for some time.
 *
 * @param time period to delay for
 */
suspend fun delay(time: Time) = delay(time.milli(Second).toLong())

/**
 * Pauses the coroutine until cancelled.
 *
 * Essentially an infinitely long delay.
 */
suspend fun freeze() = suspendCancellableCoroutine<Unit> { }

/**
 * Cancels the given function if it takes too long.
 *
 * @param time maximum time to run the function for
 * @param block function to run
 */
suspend fun withTimeout(time: Time, block: Block) {
    withTimeoutOrNull(time.milli(Second).toLong(), block)
}

/**
 * Pauses the coroutine until the predicate is true.
 *
 * @param predicate function to check
 */
suspend fun waitUntil(predicate: () -> Boolean) {
    var cont: CancellableContinuation<Unit>? = null

    @Suppress("RemoveRedundantQualifierName")
    val runOnTick = com.lynbrookrobotics.kapuchin.timing.clock.EventLoop.runOnTick {
        if (predicate() && cont?.isActive == true) {
            try {
                cont?.resume(Unit)
            } catch (e: IllegalStateException) {
            }
        }
    }

    try {
        suspendCancellableCoroutine<Unit> { cont = it }
    } finally {
        runOnTick.cancel()
    }
}