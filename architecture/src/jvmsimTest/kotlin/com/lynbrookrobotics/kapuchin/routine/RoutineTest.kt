package com.lynbrookrobotics.kapuchin.routine

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RoutineTest {

    private class RoutineTestSH : TSH<RoutineTestSH, RoutineTestC>("RoutineTest Hardware")
    private class RoutineTestC : TC<RoutineTestC, RoutineTestSH>(RoutineTestSH())

    private suspend fun RoutineTestC.countTo(n: Int) = startRoutine("count to $n") {
        log(Debug) { "Starting countTo($n)" }
        var counter = 0
        controller {
            "countTo($n)".takeIf { counter++ < n }
        }
    }

    private fun RoutineTestC.check(eight: Int, four: Int, six: Int, tolerance: Int = 0) {
        checkCount(8, eight, tolerance)
        checkCount(4, four, tolerance)
        checkCount(6, six, tolerance)
    }

    @Test(timeout = 4 * 1000)
    fun `routines run sequentially by ending themselves`() = threadDumpOnFailure {
        runBlocking {
            val c = RoutineTestC()

            c.check(0, 0, 0)
            c.countTo(8)
            c.check(8, 0, 0)
            c.countTo(4)
            c.check(8, 4, 0)
            c.countTo(6)
            c.check(8, 4, 6)
        }
    }

    @Test(timeout = 6 * 1000)
    fun `routines can still run after one times out`() = threadDumpOnFailure {
        runBlocking {
            val c = RoutineTestC()

            c.countTo(8)
            c.check(8, 0, 0)

            withTimeout(1.Second) { c.countTo(Int.MAX_VALUE) }
            c.checkCount(Int.MAX_VALUE, 10, 1)

            c.countTo(4)
            c.check(8, 4, 0)

            val j = launch { c.countTo(Int.MAX_VALUE) }
            delay(1.Second)
            j.cancel()
            c.checkCount(Int.MAX_VALUE, 20, 1)

            c.countTo(6)
            c.check(8, 4, 6)
        }
    }

    @Test(timeout = 3 * 1000)
    fun `routines can be cancelled externally`() = threadDumpOnFailure {
        val c = RoutineTestC()

        c.out.clear()
        val j1 = scope.launch {
            c.countTo(8)
        }
        while (c.routine == null) Thread.sleep(1)
        j1.cancel()
        while (c.routine != null) Thread.sleep(1)
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j2 = scope.launch {
            c.countTo(4)
        }
        while (c.routine == null) Thread.sleep(1)
        c.routine!!.cancel()
        while (c.routine != null) Thread.sleep(1)
        while (j2.isActive) Thread.sleep(1)
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j3 = scope.launch {
            launch {
                c.countTo(8)
            }.join()

            launch {
                c.countTo(4)
            }.join()

            launch {
                c.countTo(6)
            }.join()
        }
        while (c.routine == null) Thread.sleep(1)
        j3.cancel()
        while (c.routine != null) Thread.sleep(1)
        c.check(0, 0, 0, 1)

        c.out.clear()
        val j4 = scope.launch {
            launch {
                c.countTo(8)
            }.join()

            launch {
                c.countTo(4)
            }.join()

            launch {
                c.countTo(6)
            }.join()
        }
        while (c.out.count { it == "countTo(8)" } < 1) Thread.sleep(1)
        c.routine!!.cancel()
        while (j4.isActive) Thread.sleep(1)
        c.check(1, 4, 6, 1)
    }

    @Test(timeout = 2 * 1000)
    fun `routines can be cancelled internally`() = threadDumpOnFailure {
        val c = RoutineTestC()

        val j1 = scope.launch { c.countTo(8) }
        while (!j1.isActive) Thread.sleep(1)
        val j2 = scope.launch { c.countTo(4) }
        while (j1.isActive) Thread.sleep(1)
        runBlocking { j2.join() }
        c.check(0, 4, 0)
    }
}