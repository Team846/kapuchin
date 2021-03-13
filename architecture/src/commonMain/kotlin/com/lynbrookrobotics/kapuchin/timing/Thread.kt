package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CoroutineScope

/**
 * Represents the importance of something being executed
 *
 * Intended for specifying JVM and OS thread priorities. `RealTime` is the highest priority possible.
 */
enum class Priority {
    Lowest, Low, Medium, High, Highest, RealTime
}

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
expect class PlatformThread internal constructor(parent: Named, name: String, priority: Priority, run: () -> Unit)

/**
 * Thread initializer
 *
 * @receiver owner of the thread
 * @param name logging name
 * @param priority importance
 * @param run function to run
 * @return new thread running the given function
 */
fun Named.platformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)

expect inline fun <R> blockingMutex(lock: Any, block: () -> R): R
expect fun blockingDelay(time: Time)

fun Named.blockUntil(
    timeout: Time = 10.Second,
    poll: Time = 0.5.Second,
    f: () -> Boolean
): Boolean {
    if (!f()) {
        val startTime = currentTime
        while (!f() && currentTime - startTime < timeout) blockingDelay(poll)
    }
    return f()
}

expect val scope: CoroutineScope