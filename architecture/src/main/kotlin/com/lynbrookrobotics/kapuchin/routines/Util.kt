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
suspend fun CoroutineScope.runWhile(predicate: () -> Boolean, block: Block) {
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
 * Runs a block whenever the predicate is met.
 *
 * Even if the predicate turns false while the block is running, the block will not be cancelled.
 *
 * @see waitUntil
 *
 * @param predicate function to check if the block should be run.
 * @param block function to run.
 */
suspend fun CoroutineScope.whenever(predicate: () -> Boolean, block: Block) {
    while (isActive) {
        waitUntil(predicate)
        block()
    }
}

/**
 * Creates a new coroutine running blocks in parallel.
 * The coroutine ends when all blocks are complete.
 *
 * @param blocks list of functions to run in parallel.
 */
suspend fun runAll(vararg blocks: Block): Unit = supervisorScope {
    blocks.forEach {
        launch {
            it()
        }
    }
}

/**
 * Creates a new corotuine that runs `runWhile`s in a `whenever`.
 *
 * Whenever a prediate is true, its block will be run.
 * However, the block will be cancelled once its predicate returns false.
 *
 * @see runWhile
 * @see whenever
 *
 * @param blocks list of pairs of a predicate and a block.
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
 * Creates a new coroutine that launches blocks in a `whenever` block.
 *
 * Whenever a predicate is true, its block will be run.
 * The block will not be cancelled even if its predicate returns false while the block is running.
 *
 * @see whenever
 *
 * @param blocks List of pairs of a predicate and a function
 */
suspend fun launchWhenever(vararg blocks: Pair<() -> Boolean, Block>) = supervisorScope {
    blocks.forEach { (p, b) ->
        launch {
            whenever(p) {
                b()
            }
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
 * Essentially a `delay` with an infinitely long time.
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