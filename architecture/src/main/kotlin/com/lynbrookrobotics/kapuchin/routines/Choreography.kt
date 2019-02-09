package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
fun CoroutineScope.coreography(
        name: String,
        setup: FreeSensorScope.() -> suspend CoroutineScope.() -> Job
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