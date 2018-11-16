package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import kotlinx.coroutines.CoroutineScope

/**
 * Represents a thread
 *
 * All threads must belong to some parent object, have their own name, and be assigned a priority.
 *
 * @author Kunal
 * @see Named
 * @see Priority
 *
 * @param parent object this thread belongs to
 * @param name logging name
 * @param priority importance
 * @param run function to run
 */
expect class PlatformThread private constructor(parent: Named, name: String, priority: Priority, run: () -> Unit) {
    companion object {
        /**
         * Thread initializer
         *
         * @receiver owner of the thread
         * @param name logging name
         * @param priority importance
         * @param run function to run
         * @return new thread running the given function
         */
        fun Named.platformThread(name: String, priority: Priority, run: () -> Unit): PlatformThread
    }
}

expect inline fun <R> blockingMutex(lock: Any, block: () -> R): R

expect val scope: CoroutineScope