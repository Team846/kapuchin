package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.control.avg
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.subsystems.TC
import com.lynbrookrobotics.kapuchin.tests.subsystems.TSH
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.checkInSync
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Ignore
import org.junit.Test

class SensorTest {

    private class SensorTestSH : TSH<SensorTestSH, SensorTestC>("SensorTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
    }

    private object SensorTestC : TC<SensorTestC, SensorTestSH>(SensorTestSH())

    @Test(timeout = 3 * 1000)
    fun `sensors read on tick are in sync`() = runBlocking {
        val name = "sensors read on tick are in sync"
        SensorTestC.run {
            val a by hardware.sensorA.readOnTick.withStamps
            val b by hardware.sensorB.readOnTick.withStamps
            var runs = 10
            runRoutine(name) {
                checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                currentTime `is greater than?` a.stamp
                currentTime `is greater than?` b.stamp
                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `sensors read on event loop are in sync`() = runBlocking {
        val name = "sensors read on tick are in sync"
        SensorTestC.run {
            val a by hardware.sensorA.readWithEventLoop.withStamps
            val b by hardware.sensorB.readWithEventLoop.withStamps
            var lastStamp = currentTime
            var runs = 10
            runRoutine(name) {
                if (runs % 2 == 0) EventLoop.tick(currentTime)

                checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                currentTime `is greater than?` a.stamp
                currentTime `is greater than?` b.stamp

                val thisStamp = avg(a.stamp, b.stamp)
                if (runs % 2 == 1) lastStamp `is equal to?` thisStamp
                lastStamp = thisStamp

                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `sensors read eagerly are eager and efficient`() = runBlocking {
        val name = "sensors read eagerly are eager and efficient"
        SensorTestC.run {
            val a by hardware.sensorA.readEagerly.withStamps
            val b by hardware.sensorB.readEagerly.withStamps
            var runs = 10
            runRoutine(name) {
                val a1 = a
                val b1 = b
                val a2 = a
                val b2 = b

                b2 `is equal to?` b1
                a2.stamp `is greater than?` b1.stamp
                a2 `is equal to?` a1

                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `sensors are read efficiently`() = runBlocking {
        val name = "sensors are read efficiently"
        SensorTestC.run {
            val a1 by hardware.sensorA.readOnTick.withStamps
            val a2 by hardware.sensorA.readEagerly.withStamps
            val b1 by hardware.sensorB.readOnTick.withStamps
            val b2 by hardware.sensorB.readEagerly.withStamps
            var runs = 10
            runRoutine(name) {
                a1 `is equal to?` a2
                b1 `is equal to?` b2
                name.takeIf { runs-- > 0 }
            }
        }
    }

    @Ignore
    @Test(timeout = 3 * 1000)
    fun `sensors do not duplicate update lambdas`() = runBlocking {
        val name = "sensors do not duplicate runOnTick lambdas"
        SensorTestC.run {
            val ogClockJobs = clock.jobs.size
            val ogElJobs = EventLoop.jobs.size

            val a1 by hardware.sensorA.readOnTick.withStamps
            val a2 by hardware.sensorA.readOnTick.withoutStamps
            val a3 by hardware.sensorA.readOnTick.withStamps
            val a4 by hardware.sensorA.readOnTick.withoutStamps

            val a5 by hardware.sensorA.readWithEventLoop.withStamps
            val a6 by hardware.sensorA.readWithEventLoop.withoutStamps
            val a7 by hardware.sensorA.readWithEventLoop.withStamps
            val a8 by hardware.sensorA.readWithEventLoop.withoutStamps

            val b1 by hardware.sensorB.readOnTick.withStamps
            val b2 by hardware.sensorB.readOnTick.withoutStamps
            val b3 by hardware.sensorB.readOnTick.withStamps
            val b4 by hardware.sensorB.readOnTick.withoutStamps

            val b5 by hardware.sensorB.readWithEventLoop.withStamps
            val b6 by hardware.sensorB.readWithEventLoop.withoutStamps
            val b7 by hardware.sensorB.readWithEventLoop.withStamps
            val b8 by hardware.sensorB.readWithEventLoop.withoutStamps

            var runs = 10
            runRoutine(name) {
                a1 `is equal to?` a3
                a2 `is equal to?` a4
                a5 `is equal to?` a7
                a6 `is equal to?` a8

                b1 `is equal to?` b3
                b2 `is equal to?` b4
                b5 `is equal to?` b7
                b6 `is equal to?` b8

                ogClockJobs + 2 `is equal to?` clock.jobs.size
                ogElJobs + 2 `is equal to?` EventLoop.jobs.size

                name.takeIf { runs-- > 0 }
            }
        }
    }
}