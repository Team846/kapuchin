package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.min

class StandardChoreographiesTest {

    private class ChoreographyTestSH(id: Int) : TSH<ChoreographyTestSH, ChoreographyTestC>("StandardChoreographiesTest Hardware $id")
    private class ChoreographyTestC(id: Int) : TC<ChoreographyTestC, ChoreographyTestSH>(ChoreographyTestSH(id))

    @Test(timeout = 5 * 1000)
    fun `runAll launches all routines`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            runAll(
                    *comps.mapIndexed { i, c ->
                        choreography { c.countTo(i) }
                    }.toTypedArray()
            )

            comps.forEachIndexed { i, c ->
                c.checkCount(i, i)
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `runAll runs even after one job fails`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            runAll(
                    { error("This job intentionally fails") },
                    *comps.mapIndexed { i, c ->
                        choreography { c.countTo(i) }
                    }.toTypedArray()
            )

            comps.forEachIndexed { i, c ->
                c.checkCount(i, i)
            }
        }
    }

    @Test(timeout = 4 * 1000)
    fun `runAll can be cancelled externally`() = threadDumpOnFailure {
        runBlocking {
            val last = 10
            val comps = List(last + 1) { ChoreographyTestC(it) }

            val j1 = launch {
                runAll(
                        *comps.mapIndexed { i, c ->
                            choreography { c.countTo(i + 1) }
                        }.toTypedArray()
                )
            }

            while (comps.all { it.out.count { it == "countTo(${last + 1})" } < 1 }) delay(1.milli(Second))
            j1.cancelAndJoin()

            comps.forEachIndexed { i, c ->
                c.checkCount(i + 1, 1, 1)
            }

            comps.forEach { it.out.clear() }
            val j2 = launch {
                runAll(
                        *comps.mapIndexed { i, c ->
                            choreography { c.countTo(i) }
                        }.toTypedArray()
                )
            }
            while (comps[last].routine == null) delay(1.milli(Second))
            comps[last].routine!!.cancel()
            j2.join()
            comps.take(last).forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
            comps[last].checkCount(last, 0, 1)
        }
    }

    @Test(timeout = 2 * 1000)
    fun `runWhile runs only when its predicate is true`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(10) { ChoreographyTestC(it) }
            suspend fun doSomething() = runAll(
                    *comps.mapIndexed { i, c ->
                        choreography { c.countTo(i) }
                    }.toTypedArray()
            )

            runWhile({ false }, { doSomething() })
            comps.forEachIndexed { i, c -> c.checkCount(i, 0) }

            runWhile({ true }, { doSomething() })
            comps.forEachIndexed { i, c -> c.checkCount(i, i) }

            comps.forEach { it.out.clear() }

            val last = comps.size - 1
            val first = 3

            val j = launch {
                runWhile({ comps[last].out.count { it == "countTo($last)" } < first }, { doSomething() })
            }

            while (j.isActive) {
                EventLoop.tick(currentTime)
                delay(1.milli(Second))
            }
            comps.forEachIndexed { i, c -> c.checkCount(i, min(first, i), 1) }
        }
    }

    @Test(timeout = 5 * 1000)
    fun `whenever starts only when its predicate is true`() = threadDumpOnFailure {
        runBlocking {
            var didSomething: Boolean
            fun doSomething() {
                didSomething = true
            }

            var pred = false
            var j: Job? = null

            val originalRunning = EventLoop.jobsToRun.size
            try {
                j = scope.launch {
                    whenever({ pred }) {
                        doSomething()
                    }
                }
                repeat(5) {
                    didSomething = false
                    pred = true
                    EventLoop.tick(currentTime)
                    while (!didSomething) {
                        delay(1.milli(Second))
                        EventLoop.tick(currentTime)
                    }

                    while (didSomething) {
                        didSomething = false
                        pred = false
                        delay(1.milli(Second))
                        EventLoop.tick(currentTime)
                    }
                }
            } finally {
                j?.cancel()
            }
            while (originalRunning != EventLoop.jobsToRun.size) EventLoop.tick(currentTime)
        }
    }

    @Test(timeout = 5 * 1000)
    fun `runWhenever launches all routines`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            val predicates = List(15) { false }.toMutableList()
            val j = launch {
                runWhenever(
                        *List(15) { { predicates[it] } }.zip(comps.mapIndexed { i, c ->
                            choreography {
                                c.countTo(i)
                                predicates[i] = false
                            }
                        }).toTypedArray()
                )
            }

            comps.forEachIndexed { i, c ->
                c.checkCount(i, 0)
            }

            for (i in 0 until 15) {
                predicates[i] = true
            }

            while (comps[14].out.count { it == "countTo(14)" } < 14) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }

            j.cancelAndJoin()

            comps.forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
        }
    }

    @Test(timeout = 30 * 1000)
    fun `runWhenever runs even after one job fails`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            val predicates = List(15) { false }.toMutableList()
            val j = launch {
                runWhenever(
                        { true } to choreography { error("This job intentionally fails") },
                        *List(15) { { predicates[it] } }.zip(comps.mapIndexed { i, c ->
                            choreography { c.countTo(i) }
                        }).toTypedArray()
                )
            }

            comps.forEachIndexed { i, c ->
                c.checkCount(i, 0)
            }

            predicates[8] = true
            predicates[4] = true
            predicates[6] = true

            while (comps[8].out.count { it == "countTo(8)" } < 8) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[8] = false

            while (comps[4].out.count { it == "countTo(4)" } < 4 * 2 - 2) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[4] = false

            while (comps[6].out.count { it == "countTo(6)" } < 6 * 3) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[6] = false

            j.cancelAndJoin()

            comps[8].checkCount(8, 8, 1)
            comps[4].checkCount(4, 4 * 2 - 2, 1)
            comps[6].checkCount(6, 6 * 3, 1)
        }
    }

    @Test(timeout = 4 * 1000)
    fun `runWhenever can be cancelled externally`() = threadDumpOnFailure {
        runBlocking {
            val last = 10
            val comps = List(last + 1) { ChoreographyTestC(it) }
            var predicates = List(last + 1) { it % 2 == 0 }.toMutableList()

            val j1 = launch {
                runWhenever(
                        *predicates.mapIndexed { i, _ -> { predicates[i] } }
                                .zip(comps.mapIndexed { i, c ->
                                    choreography {
                                        c.countTo(i)
                                        predicates[i] = false
                                    }
                                }).toTypedArray()
                )
            }

            while (predicates.any { it }) {
                EventLoop.tick(currentTime)
                delay(20.milli(Second))
            }
            j1.cancelAndJoin()

            comps.filterIndexed { i, _ -> i % 2 == 0 }.forEachIndexed { i, c ->
                c.checkCount(i + 1, 1, 1)
            }

            comps.forEach { it.out.clear() }
            predicates = List(last + 1) { true }.toMutableList()
            val j2 = launch {
                runWhenever(
                        *predicates.mapIndexed { i, _ -> { predicates[i] } }
                                .zip(comps.mapIndexed { i, c ->
                                    choreography {
                                        c.countTo(i)
                                        predicates[i] = false
                                    }
                                }).toTypedArray()
                )
            }
            while (comps[last].routine == null) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            comps[last].routine!!.cancel()
            predicates[last] = false
            while (predicates.any { it }) {
                EventLoop.tick(currentTime)
                delay(20.milli(Second))
            }
            j2.cancelAndJoin()
            comps.take(last).forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
            comps[last].checkCount(last, 0, 1)
        }
    }

    @Test(timeout = 5 * 1000)
    fun `launchWhenever launches all routines`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            val predicates = List(15) { false }.toMutableList()
            val j = launch {
                launchWhenever(
                        *List(15) { { predicates[it] } }.zip(comps.mapIndexed { i, c ->
                            choreography {
                                c.countTo(i)
                                predicates[i] = false
                            }
                        }).toTypedArray()
                )
            }

            comps.forEachIndexed { i, c ->
                c.checkCount(i, 0)
            }

            for (i in 0 until 15) {
                predicates[i] = true
            }

            while (comps[14].out.count { it == "countTo(14)" } < 14) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }

            j.cancelAndJoin()

            comps.forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
        }
    }

    @Test(timeout = 30 * 1000)
    fun `launchWhenever runs even after one job fails`() = threadDumpOnFailure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            val predicates = List(15) { false }.toMutableList()
            val j = launch {
                launchWhenever(
                        { true } to choreography { error("This job intentionally fails") },
                        *List(15) { { predicates[it] } }.zip(comps.mapIndexed { i, c ->
                            choreography { c.countTo(i) }
                        }).toTypedArray()
                )
            }

            comps.forEachIndexed { i, c ->
                c.checkCount(i, 0)
            }

            predicates[8] = true
            predicates[4] = true
            predicates[6] = true

            while (comps[8].out.count { it == "countTo(8)" } < 8) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[8] = false

            while (comps[4].out.count { it == "countTo(4)" } < 4 * 2 - 2) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[4] = false

            while (comps[6].out.count { it == "countTo(6)" } < 6 * 3) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            predicates[6] = false

            j.cancelAndJoin()

            comps[8].checkCount(8, 8, 1)
            comps[4].checkCount(4, 4 * 2, 1)
            comps[6].checkCount(6, 6 * 3, 1)
        }
    }

    @Test(timeout = 4 * 1000)
    fun `launchWhenever can be cancelled externally`() = threadDumpOnFailure {
        runBlocking {
            val last = 10
            val comps = List(last + 1) { ChoreographyTestC(it) }
            var predicates = List(last + 1) { it % 2 == 0 }.toMutableList()

            val j1 = launch {
                launchWhenever(
                        *predicates.mapIndexed { i, _ -> { predicates[i] } }
                                .zip(comps.mapIndexed { i, c ->
                                    choreography {
                                        c.countTo(i)
                                        predicates[i] = false
                                    }
                                }).toTypedArray()
                )
            }

            while (predicates.any { it }) {
                EventLoop.tick(currentTime)
                delay(20.milli(Second))
            }
            j1.cancelAndJoin()

            comps.filterIndexed { i, _ -> i % 2 == 0 }.forEachIndexed { i, c ->
                c.checkCount(i + 1, 1, 1)
            }

            comps.forEach { it.out.clear() }
            predicates = List(last + 1) { true }.toMutableList()
            val j2 = launch {
                launchWhenever(
                        *predicates.mapIndexed { i, _ -> { predicates[i] } }
                                .zip(comps.mapIndexed { i, c ->
                                    choreography {
                                        c.countTo(i)
                                        predicates[i] = false
                                    }
                                }).toTypedArray()
                )
            }
            while (comps[last].routine == null) {
                delay(20.milli(Second))
                EventLoop.tick(currentTime)
            }
            comps[last].routine!!.cancel()
            predicates[last] = false
            while (predicates.any { it }) {
                EventLoop.tick(currentTime)
                delay(20.milli(Second))
            }
            j2.cancelAndJoin()
            comps.take(last).forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
            comps[last].checkCount(last, 0, 1)
        }
    }
}
