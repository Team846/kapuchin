package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.min

class StandardChoreographiesTest {

    private class ChoreographyTestSH(id: Int) : TSH<ChoreographyTestSH, ChoreographyTestC>("StandardChoreographiesTest Hardware $id")
    private class ChoreographyTestC(id: Int) : TC<ChoreographyTestC, ChoreographyTestSH>(ChoreographyTestSH(id))

    @Test(timeout = 2 * 1000)
    fun `runAll launches all routines`() = threadDumpOnFailiure {
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
    fun `runAll runs even after one job fails`() = threadDumpOnFailiure {
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

    @Test(timeout = 2 * 1000)
    fun `runAll can be cancelled externally`() = threadDumpOnFailiure {
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
    fun `runWhile runs only when its predicate is true`() = threadDumpOnFailiure {
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
}
