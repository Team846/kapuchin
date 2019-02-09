package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.min

class ChoreographyTest {

    private class ChoreographyTestSH(id: Int) : TSH<ChoreographyTestSH, ChoreographyTestC>("ChoreographyTest Hardware $id")
    private class ChoreographyTestC(id: Int) : TC<ChoreographyTestC, ChoreographyTestSH>(ChoreographyTestSH(id))

    @Test(timeout = 3 * 1000)
    fun `launchAll launches all routines`() = threadDumpOnFailiure {
        runBlocking {
            val comps = List(15) { ChoreographyTestC(it) }
            launchAll(
                    *comps.mapIndexed { i, c ->
                        suspend { c.countTo(i) }
                    }.toTypedArray()
            ).join()

            comps.forEachIndexed { i, c ->
                c.checkCount(i, i)
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `launchAll can be cancelled externally`() = threadDumpOnFailiure {
        runBlocking {
            val last = 10
            val comps = List(last + 1) { ChoreographyTestC(it) }

            val j1 = launchAll(
                    *comps.mapIndexed { i, c ->
                        suspend { c.countTo(i + 1) }
                    }.toTypedArray()
            )

            while (comps.all { it.out.count { it == "countTo(${last + 1})" } < 1 }) Thread.sleep(1)
            j1.cancelAndJoin()

            comps.forEachIndexed { i, c ->
                c.checkCount(i + 1, 1, 1)
            }

            comps.forEach { it.out.clear() }
            val j2 = launchAll(
                    *comps.mapIndexed { i, c ->
                        suspend { c.countTo(i) }
                    }.toTypedArray()
            )
            while (comps[last].routine == null) Thread.sleep(1)
            comps[last].routine!!.cancel()
            j2.join()
            comps.take(last).forEachIndexed { i, c ->
                c.checkCount(i, i, 1)
            }
            comps[last].checkCount(last, 0, 1)
        }
    }

    @Test(timeout = 3 * 1000)
    fun `runWhile runs only when its predicate is true`() = threadDumpOnFailiure {
        runBlocking {
            val comps = List(10) { ChoreographyTestC(it) }
            fun doSomething() = launchAll(
                    *comps.mapIndexed { i, c ->
                        suspend { c.countTo(i) }
                    }.toTypedArray()
            )

            ::doSomething runWhile { false } `is equal to?` null

            (::doSomething runWhile { true })!!.join()
            comps.forEachIndexed { i, c -> c.checkCount(i, i) }

            comps.forEach { it.out.clear() }

            val last = comps.size - 1
            val first = 3
            val j = (::doSomething runWhile { comps[last].out.count { it == "countTo($last)" } < first })!!
            while (j.isActive) {
                Thread.sleep(1)
                EventLoop.tick(currentTime)
            }
            comps.forEachIndexed { i, c -> c.checkCount(i, min(first, i), 1) }
        }
    }
}
