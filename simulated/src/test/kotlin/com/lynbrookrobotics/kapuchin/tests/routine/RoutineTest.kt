package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.delay
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.withTimeout
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.subsystems.TC
import com.lynbrookrobotics.kapuchin.tests.subsystems.TSH
import com.lynbrookrobotics.kapuchin.tests.subsystems.checkCount
import com.lynbrookrobotics.kapuchin.timing.scope
import info.kunalsheth.units.generated.Second
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RoutineTest {

    private class RoutineTestSH : TSH<RoutineTestSH, RoutineTestC>("RoutineTest Hardware")
    private object RoutineTestC : TC<RoutineTestC, RoutineTestSH>(RoutineTestSH())

    private suspend fun RoutineTestC.countTo(n: Int) = startRoutine("count to $n") {
        var counter = 0
        controller {
            "countTo($n)".takeIf { counter++ < n }
        }
    }

    private fun check(eight: Int, four: Int, six: Int, tolerance: Int = 0) {
        RoutineTestC.checkCount(8, eight, tolerance)
        RoutineTestC.checkCount(4, four, tolerance)
        RoutineTestC.checkCount(6, six, tolerance)
    }

    @Test(timeout = 3 * 1000)
    fun `routines run sequentially by ending themselves`() = runBlocking {
        RoutineTestC.out = emptyList()

        check(0, 0, 0)
        RoutineTestC.countTo(8)
        check(8, 0, 0)
        RoutineTestC.countTo(4)
        check(8, 4, 0)
        RoutineTestC.countTo(6)
        check(8, 4, 6)
    }

    @Test(timeout = 5 * 1000)
    fun `routines can still run after one times out`() = runBlocking {
        RoutineTestC.out = emptyList()

        RoutineTestC.countTo(8)
        check(8, 0, 0)

        withTimeout(1.Second) { RoutineTestC.countTo(Int.MAX_VALUE) }
        RoutineTestC.checkCount(Int.MAX_VALUE, 10, 1)

        RoutineTestC.countTo(4)
        check(8, 4, 0)

        val j = launch { RoutineTestC.countTo(Int.MAX_VALUE) }
        delay(1.Second)
        j.cancel()
        RoutineTestC.checkCount(Int.MAX_VALUE, 20, 1)

        RoutineTestC.countTo(6)
        check(8, 4, 6)
    }

    @Test(timeout = 3 * 1000)
    fun `routines can be cancelled externally`() {
        RoutineTestC.out = emptyList()

        val j1 = scope.launch { RoutineTestC.countTo(8) }
        while (RoutineTestC.routine == null) Thread.sleep(1)
        j1.cancel()
        RoutineTestC.routine `is equal to?` null
        check(0, 0, 0, 1)


        val j2 = scope.launch { RoutineTestC.countTo(4) }
        while (RoutineTestC.routine == null) Thread.sleep(1)
        RoutineTestC.routine!!.cancel()
        RoutineTestC.routine `is equal to?` null
        while (j2.isActive) Thread.sleep(1)
        check(0, 0, 0, 1)


        val j3 = scope.launch {
            RoutineTestC.countTo(8)
            RoutineTestC.countTo(4)
            RoutineTestC.countTo(6)
        }
        while (RoutineTestC.routine == null) Thread.sleep(1)
        j3.cancel()
        RoutineTestC.routine `is equal to?` null
        check(0, 0, 0, 1)


        val j4 = scope.launch {
            RoutineTestC.countTo(8)
            RoutineTestC.countTo(4)
            RoutineTestC.countTo(6)
        }
        while (RoutineTestC.out.count { it == "countTo(8)" } < 1) Thread.sleep(1)
        RoutineTestC.routine!!.cancel()
        while (j4.isActive) Thread.sleep(1)
        check(1, 4, 6, 1)
    }

    @Test(timeout = 2 * 1000)
    fun `routines can be cancelled internally`() {
        RoutineTestC.out = emptyList()

        val j1 = scope.launch { RoutineTestC.countTo(8) }
        while (!j1.isActive) Thread.sleep(1)
        val j2 = scope.launch { RoutineTestC.countTo(4) }
        while (j1.isActive) Thread.sleep(1)
        runBlocking { j2.join() }
        check(0, 4, 0)
    }
}