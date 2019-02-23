package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ChoreographyTest {

    private class ChoreographyTestSH : TSH<ChoreographyTestSH, ChoreographyTestC>("ChoreographyTest Hardware")
    private class ChoreographyTestC : TC<ChoreographyTestC, ChoreographyTestSH>(ChoreographyTestSH())

    private suspend fun ChoreographyTestC.countTo(n: Int) = startRoutine("count to $n") {
        log(Debug) { "Starting countTo($n)" }
        var counter = 0
        controller {
            "countTo($n)".takeIf { counter++ < n }
        }
    }

    private suspend fun CoroutineScope.countTo(c: ChoreographyTestC, vararg nums: Int) = startChoreo("count to ${nums.contentToString()}") {
        choreography {
            nums.forEach {
                c.countTo(it)
            }
        }
    }

    private fun ChoreographyTestC.check(eight: Int, four: Int, six: Int, tolerance: Int = 0) {
        checkCount(8, eight, tolerance)
        checkCount(4, four, tolerance)
        checkCount(6, six, tolerance)
    }

    @Test(timeout = 3 * 1000)
    fun `choreographies run sequentially by ending themselves`() = threadDumpOnFailure {
        runBlocking {
            val c = ChoreographyTestC()

            c.check(0, 0, 0)
            countTo(c, 8)
            c.check(8, 0, 0)

            countTo(c, 4, 6)
            c.check(8, 4, 6)
        }
    }

    @Test(timeout = 4 * 1000)
    fun `choreographies can still run after one times out`() = threadDumpOnFailure {
        runBlocking {
            val c = ChoreographyTestC()


            withTimeout(1.Second) { countTo(c, 8, Int.MAX_VALUE) }
            c.checkCount(8, 8, 1)
            c.checkCount(Int.MAX_VALUE, 2, 1)

            countTo(c, 4)
            c.check(8, 4, 0)

            val j = launch { countTo(c, Int.MAX_VALUE, 6) }
            delay(1.Second)
            j.cancel()
            c.checkCount(Int.MAX_VALUE, 12, 1)

            countTo(c, 6)
            c.check(8, 4, 6)
        }
    }

    @Test(timeout = 4 * 1000)
    fun `choreographies can be cancelled externally`() = threadDumpOnFailure {
        val c = ChoreographyTestC()

        c.out.clear()
        val j1 = scope.launch {
            countTo(c, 8)
        }
        while (c.routine == null) Thread.sleep(1)
        j1.cancel()
        while (c.routine != null) Thread.sleep(1)
        c.routine `is equal to?` null
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j2 = scope.launch {
            countTo(c, 4)
        }
        while (c.routine == null) Thread.sleep(1)
        c.routine!!.cancel()
        c.routine `is equal to?` null
        while (j2.isActive) Thread.sleep(1)
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j3 = scope.launch {
            launch {
                countTo(c, 8)
            }.join()

            launch {
                countTo(c, 4, 6)
            }.join()
        }
        while (c.routine == null) Thread.sleep(1)
        j3.cancel()
        c.routine `is equal to?` null
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j4 = scope.launch {
            launch {
                countTo(c, 8)
            }.join()

            launch {
                countTo(c, 4, 6)
            }.join()
        }
        while (c.out.count { it == "countTo(8)" } < 1) Thread.sleep(1)
        c.routine!!.cancel()
        while (j4.isActive) Thread.sleep(1)
        c.check(1, 4, 6, 1)
    }

    @Test(timeout = 2 * 1000)
    fun `choreographies can be cancelled internally`() = threadDumpOnFailure {
        val c = ChoreographyTestC()

        val j1 = scope.launch { countTo(c, 8) }
        while (!j1.isActive) Thread.sleep(1)
        val j2 = scope.launch { countTo(c, 4) }
        while (j1.isActive) Thread.sleep(1)
        runBlocking { j2.join() }
        c.check(0, 4, 0, 1)

        c.out.clear()
        val j3 = scope.launch { countTo(c, 8, 6) }
        while (!j3.isActive) Thread.sleep(1)
        val j4 = scope.launch { c.countTo(4) }
        while (j4.isActive) Thread.sleep(1)
        runBlocking { j3.join() }
        c.check(0, 4, 0, 1)
    }
}