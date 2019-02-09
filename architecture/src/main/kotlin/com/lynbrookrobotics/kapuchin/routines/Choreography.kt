package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private typealias Block = suspend CoroutineScope.() -> Unit

/**
 * Represents a sequence of subsystem routines
 *
 * Choreographies run all their child jobs complete, a child throws an exception, or they are cancelled.
 *
 * @author Kunal
 * @see Component
 * @see Routine
 *
 * @param C type of this subsystem's component
 * @param H type of this subsystem's hardware
 * @param Output type of this subsystem's output
 */
fun CoroutineScope.startRoutine(
        name: String,
        setup: FreeSensorScope.() -> Block
) = launch {
    val named = Named(name)
    val sensorScope = FreeSensorScope()
    try {
        val controller = sensorScope.run(setup)
        controller()
        named.log(Debug) { "Completed choreography." }
//    } catch (c: CancellationException) {
//        named.log(Debug) { "Cancelled choreography.\n${c.message}" }
//        throw c
//    } catch (t: Throwable) {
//        named.log(Error, t) { "Exception running choreography.\n${t.message}" }
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
fun CoroutineScope.choreography(controller: Block) = controller

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