package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Represents an active subsystem routine
 *
 * Routines run until their controller returns null, they throw an exception, or they are cancelled.
 *
 * @author Kunal
 * @see Component
 *
 * @param C type of this subsystem's component
 * @param H type of this subsystem's hardware
 * @param Output type of this subsystem's output
 */
class Routine<C, H, Output> internal constructor(
        parent: C, name: String,
        private val controller: C.(Time) -> Output?,
        cont: CancellableContinuation<Unit>
) :
        CancellableContinuation<Unit> by cont,
        Named by Named(name, parent),
        (C, Time) -> Output

        where C : Component<C, H, Output>,
              H : SubsystemHardware<H, C> {

    /**
     * Calculate the next subsystem output and manage this routine's lifecycle
     *
     * @param c this subsystem's component
     * @param t control loop start time
     * @return next subsystem output
     */
    override fun invoke(c: C, t: Time) =
            try {
                controller(c, t) ?: c.fallbackController(c, t).also { resume(Unit) }
            } catch (e: Throwable) {
                resumeWithException(e)
                c.fallbackController(c, t)
            }
}

private typealias Block = suspend CoroutineScope.() -> Unit

/**
 * Create a new coroutine running all `routines` in parallel
 *
 * @param blocks collection of functions to run in parallel
 * @return parent coroutine of the running routines
 */
fun CoroutineScope.launchAll(vararg blocks: Block) = startRoutine("launchAll") {
    choreography {
        blocks.forEach { launch { it() } }
    }
}

/**
 * Create a new coroutine running the function while the predicate is met
 *
 * @param sensor data source for predicate
 * @param predicate function to check if the coroutine should still be running
 * @param block function to run
 * @return coroutine which runs until `predicate` returns false
 */
fun CoroutineScope.runWhile(predicate: () -> Boolean, block: Block) = startRoutine("runWhile") {
    choreography {
        if (predicate()) {
            val job = launch { block() }

            var runOnTick: Cancel? = null
            runOnTick = EventLoop.runOnTick {
                if (!predicate()) {
                    runOnTick?.cancel()
                    job.cancel()
                }
            }

            job.join()
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
 * Cancels the given function if it takes too long
 *
 * @param time maximum time to run the function for
 * @param block function to run
 */
suspend fun withTimeout(time: Time, block: Block) =
        withTimeoutOrNull(time.milli(Second).toLong(), block)