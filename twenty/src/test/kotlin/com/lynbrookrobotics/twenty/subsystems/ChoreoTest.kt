package com.lynbrookrobotics.twenty.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*
import org.junit.Test

class ChoreoTest : Named by Named("Choreo Test") {

    @Test
    fun `ignoring jobs causes choreo to not exit`() = runBlocking {

        suspend fun choreo() = startChoreo("Test") {
            choreography {
                launch { freeze() }
                launch { freeze() }
                log(Debug) { "Reached the end choreo. Not touching the jobs" }
            }
        }

        var complete = false
        withTimeout(.1.Second) {
            choreo()
            complete = true
        }

        log(Debug) { "Choreo is ${if (!complete) "not" else ""} complete. Should not be complete" }
        assert(!complete)
    }

    @Test
    fun `returning@choreography causes choreo to not exit`() = runBlocking {

        suspend fun choreo() = startChoreo("Test") {
            choreography {
                launch { freeze() }
                launch { freeze() }
                log(Debug) { "Reached the end of choreo. returning@choreography" }

                return@choreography
            }
        }

        var complete = false
        withTimeout(.1.Second) {
            choreo()
            complete = true
        }

        log(Debug) { "Choreo is ${if (!complete) "not" else ""} complete. Should not be complete" }
        assert(!complete)
    }

    @Test
    fun `throwing CancellationException causes choreo to not exit`() = runBlocking {

        suspend fun choreo() = startChoreo("Test") {
            choreography {
                try {
                    launch { freeze() }
                    launch { freeze() }
                    log(Debug) { "Reached the end of choreo. Throwing cancellation exception and being wrapped in a try/catch." }

                    throw CancellationException()
                } catch (c: CancellationException) {
                    log(Debug) { "Caught cancellation" }
                }
            }
        }

        var complete = false
        withTimeout(.1.Second) {
            choreo()
            complete = true
        }

        log(Debug) { "Choreo is ${if (!complete) "not" else ""} complete. Should not be complete" }
        assert(!complete)
    }

    @Test
    fun `cancelling children causes choreo to exit`() = runBlocking {

        suspend fun choreo() = startChoreo("Test") {
            choreography {
                launch { freeze() }
                launch { freeze() }
                log(Debug) { "Reached the end of choreo. cancelling children" }

                coroutineContext[Job]!!.cancelChildren()
            }
        }

        var complete = false
        withTimeout(.1.Second) {
            choreo()
            complete = true
        }

        log(Debug) { "Choreo is ${if (!complete) "not " else ""}complete. Should be complete" }
        assert(complete)
    }

    @Test
    fun `explcitly cancelling jobs causes choreo to exit`() = runBlocking {

        suspend fun choreo() = startChoreo("Test") {
            choreography {
                val j1 = launch { freeze() }
                val j2 = launch { freeze() }
                log(Debug) { "Reached the end of choreo. Explicity cancelling jobs" }

                j1.cancel()
                j2.cancel()
            }
        }

        var complete = false
        withTimeout(.1.Second) {
            choreo()
            complete = true
        }

        log(Debug) { "Choreo is ${if (!complete) "not " else ""}complete. Should be complete" }
        assert(complete)
    }

    @Test
    fun `returning out of choreo works`() = runBlocking {

        var start = false
        var end = false

        suspend fun choreo() = startChoreo("Test") {

            val jobs = mutableListOf<Job>()

            choreography {
                start = true
                jobs += launch { freeze() }
                if (start) {
                    jobs.forEach { it.cancel() }
                    return@choreography
                }
                end = true
            }
        }

        choreo()

        assert(start)
        assert(!end)
    }
}