package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope

/**
 * Represents a sequence of subsystem routines
 *
 * Choreographies run until all their child jobs complete, a child throws an exception, or they are cancelled.
 *
 * @author Kunal
 * @see Component
 * @see Routine
 *
 * @param name type of this choreography
 * @param setup function returning a choreography
 */
suspend fun startChoreo(
        name: String,
        setup: FreeSensorScope.() -> Block
) {
    val named = Named(name)
    val sensorScope = FreeSensorScope()
    try {
        named.log(Debug) { "Started $name choreography." }
        val controller = sensorScope.run(setup)
        coroutineScope { controller() }
        named.log(Debug) { "Completed $name choreography." }
    } catch (c: CancellationException) {
        named.log(Debug) { "Cancelled $name choreography.\n${c.message}" }
        throw c
    } catch (t: Throwable) {
        named.log(Error, t)
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
fun choreography(controller: Block) = controller
