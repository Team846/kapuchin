package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Cancel
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.scope
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli
import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Represents an active subsystem routine
 *
 * Routines run until their `controller` returns null, they throw an exception, or they are cancelled.
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

    companion object {
        /**
         * Create a new coroutine running all `routines` in parallel
         *
         * @param routines collection of functions to run in parallel
         * @return parent coroutine of the running routines
         */
        fun launchAll(vararg routines: suspend () -> Unit) = scope.launch {
            routines.forEach { launch { it() } }
        }

        /**
         * Create a new coroutine running the function while the predicate is met
         *
         * @receiver function returning a new coroutine
         * @param predicate function to check if the coroutine should still be running
         * @return coroutine which runs until `predicate` returns false
         */
        infix fun (() -> Job).runWhile(predicate: () -> Boolean) = if (predicate()) {
            val job = this()
            var runOnTick: Cancel? = null

            runOnTick = EventLoop.runOnTick {
                if (!predicate()) {
                    runOnTick?.cancel()
                    job.cancel()
                }
            }

            job
        } else null

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
        suspend fun withTimeout(time: Time, block: suspend () -> Unit) =
                withTimeoutOrNull(time.milli(Second).toLong()) { block() }
    }
}