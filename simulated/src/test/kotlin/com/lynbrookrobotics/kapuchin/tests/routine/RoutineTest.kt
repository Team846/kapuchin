package com.lynbrookrobotics.kapuchin.tests.routine

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.subsystems.TC
import com.lynbrookrobotics.kapuchin.tests.subsystems.TSH
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeoutOrNull
import org.junit.Test

class RoutineTest {

    private class RoutineTestSH : TSH<RoutineTestSH, RoutineTestC>("RoutineTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
        override val period = 0.2.Second
        override val syncThreshold = period / 10
    }

    private object RoutineTestC : TC<RoutineTestC, RoutineTestSH>(RoutineTestSH()) {
        var out = emptyList<String>()

        override fun RoutineTestSH.output(value: String) {
            val msg = "output @ ${currentTime withDecimals 2} by thread #${Thread.currentThread().id} = $value"
            log(Debug) { msg }
            out += value
        }
    }

    private suspend fun RoutineTestC.countTo(n: Int) {
        var counter = 0
        runRoutine("Count to $n") { "countTo($n)".takeIf { counter++ < n } }
    }

    private fun check(eight: Int, four: Int, six: Int) {
        RoutineTestC.out.count { it == "countTo(8)" } `is equal to?` eight
        RoutineTestC.out.count { it == "countTo(4)" } `is equal to?` four
        RoutineTestC.out.count { it == "countTo(6)" } `is equal to?` six
    }

    @Test(timeout = 5 * 1000)
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

    @Test(timeout = 7 * 1000)
    fun `routines continue to run sequentially after a timeout`() = runBlocking {
        RoutineTestC.out = emptyList()

        RoutineTestC.countTo(8)
        check(8, 0, 0)

        withTimeoutOrNull(2000) { RoutineTestC.countTo(Int.MAX_VALUE) }
        RoutineTestC.out.count { it == "countTo(${Int.MAX_VALUE})" } `is equal to?` 10

        RoutineTestC.countTo(4)
        check(8, 4, 0)
        RoutineTestC.countTo(6)
        check(8, 4, 6)
    }

    @Test(timeout = 1 * 1000)
    fun `routines can be cancelled externally`() {
        RoutineTestC.out = emptyList()

        val j1 = launch { RoutineTestC.countTo(8) }
        while (RoutineTestC.routine == null);
        j1.cancel() `is equal to?` true
        RoutineTestC.routine `is equal to?` null
        check(0, 0, 0)


        val j2 = launch { RoutineTestC.countTo(4) }
        while (RoutineTestC.routine == null);
        RoutineTestC.routine!!.cancel()
        RoutineTestC.routine `is equal to?` null
        while (j2.isActive);
        check(0, 0, 0)


        val j3 = launch {
            RoutineTestC.countTo(8)
            RoutineTestC.countTo(4)
            RoutineTestC.countTo(6)
        }
        while (RoutineTestC.routine == null);
        j3.cancel() `is equal to?` true
        RoutineTestC.routine `is equal to?` null
        check(0, 0, 0)


        val j4 = launch {
            RoutineTestC.countTo(8)
            RoutineTestC.countTo(4)
            RoutineTestC.countTo(6)
        }
        while (RoutineTestC.routine == null);
        RoutineTestC.routine!!.cancel()
        RoutineTestC.routine `is equal to?` null
        while (j4.isActive);
        check(0, 0, 0)
    }

    @Test(timeout = 1 * 1000)
    fun `routines can be cancelled internally`() {
        RoutineTestC.out = emptyList()

        val j1 = launch { RoutineTestC.countTo(8) }
        while (!j1.isActive);
        val j2 = launch { RoutineTestC.countTo(4) }
        while (j1.isActive);
        runBlocking { j2.join() }
        check(0, 4, 0)
    }
}