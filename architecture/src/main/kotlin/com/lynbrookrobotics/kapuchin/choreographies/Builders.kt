package com.lynbrookrobotics.kapuchin.choreographies

import com.lynbrookrobotics.kapuchin.routines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
fun CoroutineScope.whenever(predicate: () -> Boolean, block: Block) = launch {
    while (isActive) {
        waitUntil(predicate)
        block()
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
fun CoroutineScope.runWhenever(vararg blocks: Pair<() -> Boolean, Block>) = launch {
    blocks.forEach { (p, b) ->
        whenever(p) {
            runWhile(p) {
                try {
                    b()
                } catch (e: Exception) {
                }
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
fun CoroutineScope.launchWhenever(vararg blocks: Pair<() -> Boolean, Block>) = launch {
    blocks.forEach { (p, b) ->
        whenever(p) {
            try {
                b()
            } catch (e: Exception) {
            }
        }
    }
}